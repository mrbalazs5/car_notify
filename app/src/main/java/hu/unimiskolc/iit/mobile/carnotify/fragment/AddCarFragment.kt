package hu.unimiskolc.iit.mobile.carnotify.fragment

import android.app.Activity
import android.app.DatePickerDialog
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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

    private lateinit var saveDir: File

    private var lastInspectionCalendar: Calendar = Calendar.getInstance()

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data

            selectedImageUri = data?.data
            binding.uploadImage.setImageURI(selectedImageUri)

            if (selectedImageUri == null) {
                binding.root.snackbar("Select an Image First")
                return@registerForActivityResult
            }

            val parcelFileDescriptor =
                contentResolver.openFileDescriptor(selectedImageUri!!, "r", null) ?: return@registerForActivityResult

            val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
            val file = File(saveDir, contentResolver.getFileName(selectedImageUri!!))
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)

            selectedImageFilePath = file.path
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AddCarFragmentBinding.inflate(inflater, container, false)

        contentResolver = this.requireContext().contentResolver
        saveDir = this.requireContext().filesDir

        val db = Room.databaseBuilder(
            this.requireContext(),
            CarNotifyDatabase::class.java, "car_notify.db"
        ).build()

        carDataSource = RoomCarDataSource(db.carDao(), CarMapper())

        return binding.root
    }

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

        val dateSetListener = DatePickerDialog.OnDateSetListener {
            _, year, monthOfYear, dayOfMonth ->
                val formatter = SimpleDateFormat("yyyy.MM.dd", Locale.US)

                lastInspectionCalendar.set(Calendar.YEAR, year)
                lastInspectionCalendar.set(Calendar.MONTH, monthOfYear)
                lastInspectionCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                binding.lastInspectionValue.text = formatter.format(lastInspectionCalendar.time)
        }

        binding.lastInspection.setOnClickListener {
            DatePickerDialog(this.requireContext(),
                dateSetListener,
                lastInspectionCalendar.get(Calendar.YEAR),
                lastInspectionCalendar.get(Calendar.MONTH),
                lastInspectionCalendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.submitButton.setOnClickListener {
            uiScope.launch {
                val type: String = binding.type.text.toString()
                val licensePlate: String = binding.licensePlate.text.toString()
                val cylinderCapacity: String = binding.cylinderCapacity.text.toString()
                val enginePower: String = binding.enginePower.text.toString()
                val horsepower: String = binding.horsepower.text.toString()
                val totalMass: String = binding.totalMass.text.toString()
                val ownMass: String = binding.ownMass.text.toString()
                val propellant = binding.propellant.selectedItem as Propellant

                if(
                   type == "" ||
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

                val user = arguments?.get("user") as User

                val car = Car(
                    0,
                    type,
                    selectedImageFilePath,
                    user,
                    lastInspectionCalendar.time,
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
            resultLauncher.launch(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        selectedImageUri = null
    }
}