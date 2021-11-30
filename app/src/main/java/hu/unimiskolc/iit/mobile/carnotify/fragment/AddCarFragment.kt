package hu.unimiskolc.iit.mobile.carnotify.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.room.Room
import hu.unimiskolc.iit.mobile.carnotify.R
import hu.unimiskolc.iit.mobile.carnotify.databinding.AddCarFragmentBinding
import hu.unimiskolc.iit.mobile.carnotify.snackbar
import hu.unimiskolc.iit.mobile.carnotify.getFileName
import hu.unimiskolc.iit.mobile.core.domain.Car
import hu.unimiskolc.iit.mobile.core.domain.Propellant
import hu.unimiskolc.iit.mobile.core.domain.User
import hu.unimiskolc.iit.mobile.framework.db.CarNotifyDatabase
import hu.unimiskolc.iit.mobile.framework.db.datasource.RoomCarDataSource
import hu.unimiskolc.iit.mobile.framework.db.mapper.CarMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class AddCarFragment: Fragment() {
    private var _binding: AddCarFragmentBinding? = null

    private val binding get() = _binding!!

    private val viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private lateinit var carDataSource: RoomCarDataSource

    private var selectedImageUri: Uri? = null

    private lateinit var selectedImageFilePath: String

    private lateinit var contentResolver: ContentResolver

    private lateinit var cacheDir: File

    companion object {
        const val REQUEST_CODE_PICK_IMAGE = 101
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AddCarFragmentBinding.inflate(inflater, container, false)

        contentResolver = this.requireContext().contentResolver
        cacheDir = this.requireContext().cacheDir

        val db = Room.databaseBuilder(
            this.requireContext(),
            CarNotifyDatabase::class.java, "car_notify.db"
        ).build()

        carDataSource = RoomCarDataSource(db.carDao(), CarMapper())

        return binding.root
    }

    @SuppressLint("SimpleDateFormat")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.propellant.adapter =
            ArrayAdapter(
                this.requireContext(),
                R.layout.spinner_layout,
                Propellant.values()
            )

        binding.uploadImage.setOnClickListener {
            openImageChooser()
        }


        binding.submitButton.setOnClickListener {
            uiScope.launch {
                val parser =  SimpleDateFormat("yyyy-MM-dd")

                val type: String = binding.type.text.toString()
                val lastInspectionString: String = binding.lastInspection.text.toString()
                val licensePlate: String = binding.licensePlate.text.toString()
                val cylinderCapacity: String = binding.cylinderCapacity.text.toString()
                val enginePower: String = binding.enginePower.text.toString()
                val horsepower: String = binding.horsepower.text.toString()
                val totalMass: String = binding.totalMass.text.toString()
                val ownMass: String = binding.ownMass.text.toString()
                val propellant = binding.propellant.selectedItem as Propellant

                if(
                   type == "" ||
                   lastInspectionString == "" ||
                   licensePlate == "" ||
                   cylinderCapacity == "" ||
                   enginePower == "" ||
                   horsepower == "" ||
                   totalMass == "" ||
                   ownMass == ""
                ) {
                    Toast.makeText(
                        requireContext(),
                        "Missing form data.",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@launch
                }

                val lastInspectionFormatted: Date = parser.parse(lastInspectionString)!!

                val user = arguments?.get("user") as User

                val car = Car(
                    0,
                    type,
                    selectedImageFilePath,
                    user,
                    lastInspectionFormatted,
                    licensePlate,
                    cylinderCapacity.toInt(),
                    enginePower.toInt(),
                    horsepower.toInt(),
                    totalMass.toInt(),
                    ownMass.toInt(),
                    propellant
                )

                carDataSource.add(car)

                Toast.makeText(
                    requireContext(),
                    "New car added successfully.",
                    Toast.LENGTH_SHORT
                ).show()

                findNavController()
                    .navigate(
                        R.id.action_AddCarFragment_to_MyCarsFragment,
                        bundleOf(Pair("user", user))
                    )

                return@launch
            }
        }
    }

    private fun openImageChooser() {
        Intent(Intent.ACTION_PICK).also {
            it.type = "image/*"
            val mimeTypes = arrayOf("image/jpeg", "image/png")
            it.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            startActivityForResult(it, REQUEST_CODE_PICK_IMAGE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_PICK_IMAGE -> {
                    selectedImageUri = data?.data
                    binding.uploadImage.setImageURI(selectedImageUri)

                    if (selectedImageUri == null) {
                        binding.root.snackbar("Select an Image First")
                        return
                    }

                    val parcelFileDescriptor =
                        contentResolver.openFileDescriptor(selectedImageUri!!, "r", null) ?: return

                    val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
                    val file = File(cacheDir, contentResolver.getFileName(selectedImageUri!!))
                    val outputStream = FileOutputStream(file)
                    inputStream.copyTo(outputStream)

                    selectedImageFilePath = file.path
                }
            }
        }
    }
}