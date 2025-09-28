package expo.modules.easyfs

import expo.modules.kotlin.Promise
import java.io.IOException
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import android.os.Environment
import android.os.Build
import android.provider.MediaStore
import android.content.ContentResolver
import android.content.ContentValues
import android.webkit.MimeTypeMap
import android.Manifest
import android.content.pm.PackageManager
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.FileInputStream

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






    AsyncFunction("copyFileToDownload") { uriOrPath: String, filename: String, promise: Promise ->
      // NOTE: This method now supports all Android versions:
      //  - API < 29: direct file write to public Downloads (requires WRITE_EXTERNAL_STORAGE permission)
      //  - API >=29: uses MediaStore Download collection (no legacy storage permission required)
      val reactContext = appContext.reactContext
      if (reactContext == null) {
        promise.reject("ERR_CONTEXT_NULL", "React context is null", null)
        return@AsyncFunction
      }

      try {
        val contentResolver: ContentResolver = reactContext.contentResolver

        // Get InputStream from either a content:// URI, file:// URI, or raw path
        val inputStream: InputStream? = when {
          uriOrPath.startsWith("content://") -> {
            contentResolver.openInputStream(android.net.Uri.parse(uriOrPath))
          }
          uriOrPath.startsWith("file://") -> {
            FileInputStream(uriOrPath.removePrefix("file://"))
          }
          else -> {
            // treat as raw path
            val f = File(uriOrPath)
            if (f.exists()) FileInputStream(f) else null
          }
        }

        if (inputStream == null) {
          promise.reject("ERR_FILE_NOT_FOUND", "Unable to open input stream for: $uriOrPath", null)
          return@AsyncFunction
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          // Scoped storage path via MediaStore (Android 10+)
          val downloadsCollection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

          val mimeType = guessMimeTypeFromName(filename)
          val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            // RELATIVE_PATH lets us specify Downloads root; you may append a subfolder e.g. "Downloads/myApp" by adding "/myApp"
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
          }

          val itemUri = contentResolver.insert(downloadsCollection, values)
            ?: run {
              promise.reject("ERR_MEDIASTORE_INSERT", "Failed to create download entry", null)
              return@AsyncFunction
            }

          try {
            contentResolver.openOutputStream(itemUri)?.use { output ->
              inputStream.use { it.copyTo(output) }
            } ?: run {
              promise.reject("ERR_OPEN_OUTPUT", "Failed to open output stream in MediaStore", null)
              return@AsyncFunction
            }
            // Mark as not pending so it's visible to user
            values.clear()
            values.put(MediaStore.MediaColumns.IS_PENDING, 0)
            contentResolver.update(itemUri, values, null, null)
            promise.resolve("File saved to public Downloads via MediaStore URI: $itemUri")
          } catch (e: Exception) {
            promise.reject("ERR_MEDIASTORE_WRITE", e.message, e)
          }
        } else {
          // Legacy external storage write (Android 9 and below)
          if (reactContext.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            promise.reject("ERR_PERMISSION_DENIED", "WRITE_EXTERNAL_STORAGE permission is required for API <29", null)
            return@AsyncFunction
          }

            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) downloadsDir.mkdirs()
            val outFile = File(downloadsDir, filename)
            try {
              FileOutputStream(outFile).use { output ->
                inputStream.use { it.copyTo(output) }
              }
              promise.resolve("File copied successfully to: ${outFile.absolutePath}")
            } catch (e: IOException) {
              promise.reject("ERR_FILE_COPY_FAILED", "Failed to copy file: ${e.message}", e)
            }
        }
      } catch (e: Exception) {
        promise.reject("ERR_UNEXPECTED", e.message, e)
      }
    }
  }
}

// Helper to guess mime type (basic) based on filename
private fun guessMimeTypeFromName(name: String): String {
  val extension = name.substringAfterLast('.', "").lowercase()
  if (extension.isEmpty()) return "application/octet-stream"
  val map = MimeTypeMap.getSingleton()
  return map.getMimeTypeFromExtension(extension) ?: "application/octet-stream"
}