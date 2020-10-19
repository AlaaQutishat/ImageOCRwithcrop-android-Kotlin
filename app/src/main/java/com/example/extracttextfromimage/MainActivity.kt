package com.example.extracttextfromimage

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.Nullable
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream

class MainActivity : AppCompatActivity() {
    lateinit var buttonGallery: Button
    lateinit var copy: Button
    lateinit var extracttext: Button
    lateinit var crop: Button
    lateinit var recognizeText: TextView
    lateinit var captureImage: ImageView
    lateinit var bitmap: Bitmap
    lateinit var uri: Uri
    var click:Int =0
    var extract:Int =0
    var REQUEST_FOR_IMAGE_FROM_GALLERY :Int= 101
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        buttonGallery=findViewById(R.id.button_gallery)
        copy = findViewById(R.id.copy)
        extracttext = findViewById(R.id.extracttext)
        crop = findViewById(R.id.cropimage)
        recognizeText = findViewById(R.id.text)
        captureImage = findViewById(R.id.imageView)
        buttonGallery.setOnClickListener(object: View.OnClickListener {
            override fun onClick(view:View) {
                openGallery()
            }
        })
        copy.setOnClickListener(object: View.OnClickListener {
            override fun onClick(view:View) {
                if(extract == 1 && click == 1 ) {
                    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("label", recognizeText.getText())
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(
                        getApplicationContext(),
                        "Copied to Clipboard!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else  if (extract == 0 && click == 1){
                    Toast.makeText(getApplicationContext(), "Extract Text First ", Toast.LENGTH_SHORT).show()
                }
                else  if (extract == 0 && click == 0){
                    Toast.makeText(getApplicationContext(), "Get Image From Gallery First then Extract Text  ", Toast.LENGTH_SHORT).show()
                }
            }
        })
        extracttext.setOnClickListener(object: View.OnClickListener {
            override fun onClick(view:View) {
                if (click == 1) {
                extracttext()
                    extract = 1 }
                else {
                    Toast.makeText(getApplicationContext(), "Get Image From Gallery First", Toast.LENGTH_SHORT).show()

                }

            }
        })

        crop.setOnClickListener(object: View.OnClickListener {
            override fun onClick(view:View) {
                if (click == 1) {
                    CropImage.activity(uri)
                        .setGuidelines(CropImageView.Guidelines.OFF)
                        .setMultiTouchEnabled(true)
                        .start(this@MainActivity)


                }
                else {
                    Toast.makeText(getApplicationContext(), "Get Image From Gallery First", Toast.LENGTH_SHORT).show()

                }
            }
        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode === CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            val result = CropImage.getActivityResult(data)
            if (resultCode === RESULT_OK)
            {

                val resultUri = result.getUri()
                bitmap = decodeUriToBitmap(this , resultUri)

                captureImage.setImageBitmap(bitmap)

            }
            else if (resultCode === CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
            {
                val error = result.getError()
            }
        }
        if (requestCode == REQUEST_FOR_IMAGE_FROM_GALLERY && resultCode == RESULT_OK)
        {
            uri = data?.getData()!!
            try
            {
               bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri)
                captureImage.setImageBitmap(bitmap)
                click=1
            }
            catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun decodeUriToBitmap(mContext:Context, sendUri:Uri):Bitmap {
        var getBitmap: Bitmap? = null
        try
        {
            val image_stream: InputStream
            try
            {
                image_stream = mContext.getContentResolver().openInputStream(sendUri)!!
                getBitmap = BitmapFactory.decodeStream(image_stream)
            }
            catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }
        catch (e:Exception) {
            e.printStackTrace()
        }
        return getBitmap!!
    }

    private fun openGallery() {
        val photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.setType("image/*")
        startActivityForResult(photoPickerIntent, REQUEST_FOR_IMAGE_FROM_GALLERY)
    }

    private fun extracttext() {
        val txtRecognizer = TextRecognizer.Builder(getApplicationContext()).build()
        if (!txtRecognizer.isOperational())
        {
            recognizeText.setText("Error")
        }
        else
        {
            val frame = Frame.Builder().setBitmap(bitmap).build()
            val items = txtRecognizer.detect(frame)
            val strBuilder = StringBuilder()
            for (i in 0 until items.size())
            {
                val item = items.valueAt(i) as TextBlock
                strBuilder.append(item.getValue())
                strBuilder.append("/")
                for (line in item.getComponents())
                {
                    //extract scanned text lines here
                    Log.v("lines", line.getValue())
                    for (element in line.getComponents())
                    {
                        //extract scanned text words here
                        Log.v("element", element.getValue())
                    }
                }
            }
            recognizeText.setText(strBuilder.toString().substring(0, strBuilder.toString().length - 1))
        }
    }

}