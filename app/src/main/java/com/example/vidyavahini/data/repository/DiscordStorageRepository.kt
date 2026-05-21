package com.example.vidyavahini.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream

data class DiscordUploadResult(
    val url: String,
    val messageId: String
)

class DiscordStorageRepository {

    private val client = OkHttpClient()
    private val WEBHOOK_BASE_URL = "https://discord.com/api/webhooks/1506768923578732624/Qgi33LlTcwC3IGtf_-HGZB5B2I2PYBclyN60pd5gb8umecT7IYHbBdyrOS96uKoq0QrF"
    private val DISCORD_WEBHOOK_URL = "$WEBHOOK_BASE_URL?wait=true"

    suspend fun uploadBusImage(context: Context, imageUri: Uri): DiscordUploadResult? {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Load and Crop the image
                val croppedByteArray = processAndCropImage(context, imageUri) ?: return@withContext null

                // 2. Build the Multi-part Form Request payload
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "file", 
                        "bus_proof_${System.currentTimeMillis()}.jpg",
                        croppedByteArray.toRequestBody("image/jpeg".toMediaTypeOrNull(), 0, croppedByteArray.size)
                    )
                    .build()

                val request = Request.Builder()
                    .url(DISCORD_WEBHOOK_URL)
                    .post(requestBody)
                    .build()

                // 3. Execute request and parse the response link
                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string() ?: ""
                    if (response.isSuccessful) {
                        val jsonObject = JSONObject(responseBody)
                        val attachments = jsonObject.getJSONArray("attachments")
                        val url = attachments.getJSONObject(0).getString("url")
                        val messageId = jsonObject.getString("id")
                        android.util.Log.d("DiscordUpload", "Upload successful: $url, MessageID: $messageId")
                        DiscordUploadResult(url, messageId)
                    } else {
                        android.util.Log.e("DiscordUpload", "Failed: ${response.code} - $responseBody")
                        null
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("DiscordUpload", "Error uploading", e)
                null
            }
        }
    }

    suspend fun deleteBusImage(messageId: String?): Boolean {
        if (messageId.isNullOrEmpty()) return true
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("$WEBHOOK_BASE_URL/messages/$messageId")
                    .delete()
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        android.util.Log.d("DiscordDelete", "Delete successful for: $messageId")
                        true
                    } else {
                        android.util.Log.e("DiscordDelete", "Failed to delete: ${response.code}")
                        false
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("DiscordDelete", "Error deleting", e)
                false
            }
        }
    }

    private fun processAndCropImage(context: Context, uri: Uri): ByteArray? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) return null

            // Target aspect ratio 1:1 as requested
            val targetWidth = 1000
            val targetHeight = 1000
            
            val originalWidth = originalBitmap.width
            val originalHeight = originalBitmap.height
            
            val targetRatio = targetWidth.toFloat() / targetHeight.toFloat()
            val originalRatio = originalWidth.toFloat() / originalHeight.toFloat()
            
            var cropWidth = originalWidth
            var cropHeight = originalHeight
            var xOffset = 0
            var yOffset = 0
            
            if (originalRatio > targetRatio) {
                // Image is wider than target - crop sides
                cropWidth = (originalHeight * targetRatio).toInt()
                xOffset = (originalWidth - cropWidth) / 2
            } else {
                // Image is taller than target - crop top/bottom
                cropHeight = (originalWidth / targetRatio).toInt()
                yOffset = (originalHeight - cropHeight) / 2
            }
            
            val croppedBitmap = Bitmap.createBitmap(originalBitmap, xOffset, yOffset, cropWidth, cropHeight)
            val scaledBitmap = Bitmap.createScaledBitmap(croppedBitmap, targetWidth, targetHeight, true)
            
            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            
            // Cleanup
            if (originalBitmap != croppedBitmap) originalBitmap.recycle()
            croppedBitmap.recycle()
            // scaledBitmap is what we return as bytes, then recycle if needed or let GC handle
            
            outputStream.toByteArray()
        } catch (e: Exception) {
            android.util.Log.e("DiscordUpload", "Error cropping image", e)
            null
        }
    }
}
