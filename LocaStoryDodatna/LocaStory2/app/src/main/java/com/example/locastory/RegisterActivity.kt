package com.example.locastory

import android.Manifest
import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RegisterActivity : AppCompatActivity() {

    private lateinit var userName: EditText
    private lateinit var firstName: EditText
    private lateinit var lastName: EditText
    private lateinit var phoneNumber: EditText
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var passwordAgain: EditText
    private lateinit var profilePic: ImageButton
    private lateinit var register: Button
    private lateinit var auth: FirebaseAuth

    private var currentPhotoPath: String? = null
    private val CAMERA_PERMISSION_REQUEST_CODE = 100
    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val imageBitmap = result.data?.extras?.get("data") as Bitmap
                profilePic.setImageBitmap(imageBitmap)
            }
        }
    /*private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                //launchCamera()
                val imageBitmap = result.data?.extras?.get("data") as Bitmap
                profilePic.setImageBitmap(imageBitmap)
            }
        }*/


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        userName = findViewById(R.id.etUserName)
        firstName = findViewById(R.id.etFName)
        lastName = findViewById(R.id.etLName)
        phoneNumber = findViewById(R.id.etPhoneNumber)
        email = findViewById(R.id.etEmail)
        password = findViewById(R.id.etPassword)
        passwordAgain = findViewById(R.id.etPasswordAgain)
        profilePic = findViewById(R.id.ibtnProfilePic)
        register = findViewById(R.id.bRegister)
        auth = FirebaseAuth.getInstance()

        /*if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            launchCamera()
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
*/

        if(checkCameraPerm(this))
        {
            Toast.makeText(this,"Camera permission granted!",Toast.LENGTH_SHORT).show()
        } else {
            requestCameraPerm(this)
        }

        profilePic.setOnClickListener {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            takePictureLauncher.launch(cameraIntent)
        }


        register.setOnClickListener{
            val emailText = email.text.toString().trim()
            val passwordText = password.text.toString()
            val passwordAgainText = passwordAgain.text.toString()
            val usernameText = userName.text.toString().trim()
            val fNameText = firstName.text.toString().trim()
            val lNameText = lastName.text.toString().trim()
            val phoneNumberText = phoneNumber.text.toString().trim()
            val profilePicDrawable = profilePic.drawable as BitmapDrawable?
            val profileBitmap = profilePicDrawable?.bitmap


            if(emailText.isBlank() || passwordText.isBlank() || passwordAgainText.isBlank()  || usernameText.isBlank()
                || fNameText.isBlank() || lNameText.isBlank() || phoneNumberText.isBlank()) {
                Toast.makeText(this, "Empty credentials!", Toast.LENGTH_SHORT).show()
            }
            else if(passwordText.length < 8 ) {
                Toast.makeText(this, "Password must have minimum of 8 characters!", Toast.LENGTH_SHORT).show()
            }
            else if(!isValidEmail(emailText)) {
                Toast.makeText(this, "Please enter a valid email!", Toast.LENGTH_SHORT).show()
            }
            else if(!isValidPhone(phoneNumberText)) {
                Toast.makeText(this, "Please enter a valid phone number!", Toast.LENGTH_SHORT).show()
            }
            else if(profileBitmap == null) {
                Toast.makeText(this, "Please provide a picture!", Toast.LENGTH_SHORT).show()
            }
            else {
                registerUser(emailText, usernameText, passwordText, fNameText, lNameText ,phoneNumberText, profileBitmap)
            }
        }

    }

    private fun checkCameraPerm(activity: Activity) : Boolean
    {
        val cameraPerm = Manifest.permission.CAMERA
        val permStatus = ContextCompat.checkSelfPermission(activity,cameraPerm)

        return permStatus == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPerm(activity: Activity)
    {
        val cameraPerm = Manifest.permission.CAMERA
        ActivityCompat.requestPermissions(activity, arrayOf(cameraPerm),CAMERA_PERMISSION_REQUEST_CODE)
    }

   /*
    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                launchCamera()
            } else {
                Toast.makeText(this, "Camera permission not granted. Cannot access the camera.", Toast.LENGTH_SHORT).show()
            }
        }
*/
    private fun launchCamera() {
        // Create a new file to store the captured image.
        val photoFile = createImageFile()
        val photoUri = FileProvider.getUriForFile(
            this,
            "${packageName}.provider",
            photoFile
        )
        //takePictureLauncher.launch(photoUri)
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    /*override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    profilePic.setOnClickListener {
                        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        takePictureLauncher.launch(cameraIntent)
                    }
                } else {
                    Toast.makeText(this,"Camera permission denied!",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }*/

    private fun registerUser(emailText: String, usernameText: String, passwordText: String,fNameText: String,lNameText: String,
                             phoneNumberText: String, profileBitmap: Bitmap) {



        auth.createUserWithEmailAndPassword(emailText, passwordText)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userId = user?.uid


                    if (userId != null) {
                        val database = FirebaseDatabase.getInstance()
                        val usersRef = database.reference.child("users")

                        val userData = HashMap<String, Any>()
                        userData["email"] = emailText
                        userData["username"] = usernameText
                        userData["firstName"]= fNameText
                        userData["lastName"]= lNameText
                        userData["phoneNumber"]=phoneNumberText
                        userData["score"] = 0

                        val storageRef = FirebaseStorage.getInstance().reference
                        val profilePicRef = storageRef.child("profile_pictures/$userId.jpg")
                        val baos = ByteArrayOutputStream()
                        profileBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                        val data = baos.toByteArray()

                        val uploadTask = profilePicRef.putBytes(data)
                        uploadTask.addOnSuccessListener { taskSnapshot ->
                            val downloadUrlTask = taskSnapshot.storage.downloadUrl
                            downloadUrlTask.addOnSuccessListener { uri ->
                                val profilePicUrl = uri.toString()
                                userData["profilePicUrl"] = profilePicUrl

                                usersRef.child(userId).setValue(userData)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { _ ->
                                        Toast.makeText(this, "Failed to store user data", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }.addOnFailureListener { _ ->
                            Toast.makeText(this,"Failed to upload profile picture", Toast.LENGTH_SHORT).show()
                        }

                        usersRef.child(userId).setValue(userData)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { _ ->
                                Toast.makeText(this, "Failed to store user data", Toast.LENGTH_SHORT).show()
                            }
                    }

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()

                } else {
                    val exception = task.exception
                    Toast.makeText(this, "Failed to create user account: ${exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }


    private fun isValidPhone(phone: String): Boolean {
        val phoneNumberRegex = Regex("^\\+[1-9]\\d{1,14}\$")
        return phone.matches(phoneNumberRegex)
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex("^\\w+([.-]?\\w+)*@\\w+([.-]?\\w+)*(\\.\\w{2,3})+\$")
        return email.matches(emailRegex)
    }
}