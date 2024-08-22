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
  override fun definition() = ModuleDefinition {
    Name("ExpoEasyFs")

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