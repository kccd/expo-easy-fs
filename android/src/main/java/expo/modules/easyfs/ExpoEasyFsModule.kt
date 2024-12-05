package expo.modules.easyfs

import expo.modules.kotlin.Promise
import java.io.IOException
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import android.os.Environment

import android.content.ContentResolver
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class ExpoEasyFsModule : Module() {

  /**
   * 递归删除目录及其子文件
   */
  private fun deleteDirectoryRecursively(directory: File): Boolean {
      if (!directory.isDirectory) return false

      val files = directory.listFiles()
      if (files != null) {
          for (file in files) {
              if (file.isDirectory) {
                  if (!deleteDirectoryRecursively(file)) {
                      return false
                  }
              } else {
                  if (!file.delete()) {
                      return false
                  }
              }
          }
      }

      return directory.delete()
  }

  override fun definition() = ModuleDefinition {
    Name("ExpoEasyFs")
    // 获取可以操作的目录
    AsyncFunction("getPaths") { promise: Promise ->
      try {
        val downloadsPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
        promise.resolve(mapOf(
          "downloads" to downloadsPath
        ))
      } catch (e: Exception) {
        // 捕获错误并返回给 JavaScript
        promise.reject("ERROR", e.message, e)
      }
    }
    // 创建目录
    AsyncFunction("mkdir") { dirPath: String, promise: Promise ->
      try {
        val dir = File(dirPath)
        if (dir.exists() && dir.isDirectory) {
          // 如果目录已存在
          promise.resolve("Directory already exists: $dirPath")
        } else {
          // 尝试创建目录
          val created = dir.mkdirs()
          if (created) {
            promise.resolve("Directory created successfully: $dirPath")
          } else {
            promise.reject("MAKE_DIR_ERROR", "Failed to create directory: $dirPath", null)
          }
        }
      } catch (e: Exception) {
        // 捕获任何异常并返回错误信息
        promise.reject("ERROR", e.message, e)
      }
    }
    // 复制文件
    AsyncFunction("copyFile") { sourcePath: String, destinationPath: String, promise: Promise ->
      try {
        val sourceFile = File(sourcePath)
        val destinationFile = File(destinationPath)

        // 检查源文件是否存在
        if (!sourceFile.exists()) {
            promise.reject("SOURCE_FILE_NOT_FOUND", "Source file does not exist: $sourcePath", null)
            return@AsyncFunction
        }

        // 确保目标路径的父目录存在
        val parentDir = destinationFile.parentFile
        if (parentDir != null && !parentDir.exists()) {
            val created = parentDir.mkdirs()
            if (!created) {
                promise.reject("ERROR", "Failed to create parent directory for destination: $destinationPath", null)
                return@AsyncFunction
            }
        }

        // 使用文件流进行文件复制
        sourceFile.inputStream().use { input ->
            destinationFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        promise.resolve("File copied successfully: $sourcePath -> $destinationPath")
      } catch (e: Exception) {
        // 捕获异常并返回错误
        promise.reject("ERROR", e.message, e)
      }
    }

    AsyncFunction("exists") { path: String, promise: Promise ->
      try {
        val file = File(path)

        // 检查路径或文件是否存在
        if (file.exists()) {
            // 返回路径的类型（文件或目录）
            val type = when {
                file.isDirectory -> "directory"
                file.isFile -> "file"
                else -> "unknown"
            }
            promise.resolve(mapOf(
                "exists" to true,
                "type" to type,
                "path" to path
            ))
        } else {
            promise.resolve(mapOf(
                "exists" to false,
                "path" to path
            ))
        }
      } catch (e: Exception) {
        // 捕获异常并返回错误
        promise.reject("ERROR", e.message, e)
      }
    }


    AsyncFunction("remove") { path: String, promise: Promise ->
      try {
        val file = File(path)

        if (!file.exists()) {
            // 如果路径不存在
            promise.reject("ERROR", "File or directory does not exist: $path", null)
            return@AsyncFunction
        }

        val success = if (file.isDirectory) {
            deleteDirectoryRecursively(file) // 递归删除目录
        } else {
            file.delete() // 删除文件
        }

        if (success) {
            promise.resolve("Deleted successfully: $path")
        } else {
            promise.reject("ERROR", "Failed to delete: $path", null)
        }
      } catch (e: Exception) {
        // 捕获异常并返回错误信息
        promise.reject("ERROR", e.message, e)
      }
    }






    AsyncFunction("copyFileToDownload") { uri: String, filename: String, promise: Promise ->
      val reactContext = appContext.reactContext

      if (reactContext == null) {
        promise.reject("ERR_CONTEXT_NULL", "React context is null", null)
        return@AsyncFunction
      }
      try {
        val contentResolver: ContentResolver = reactContext.contentResolver
        val inputStream: InputStream? = contentResolver.openInputStream(android.net.Uri.parse(uri))
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, filename)

        if (inputStream != null) {
          val outputStream = FileOutputStream(file)
          val buffer = ByteArray(1024)
          var bytesRead: Int

          while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            outputStream.write(buffer, 0, bytesRead)
          }

          inputStream.close()
          outputStream.close()
          promise.resolve("File copied successfully to: ${file.absolutePath}")
        } else {
          promise.reject("ERR_FILE_NOT_FOUND", "Input stream is null", null)
        }
      } catch (e: IOException) {
        promise.reject("ERR_FILE_COPY_FAILED", "Failed to copy file: ${e.message}", e)
      }
    }
  }
}