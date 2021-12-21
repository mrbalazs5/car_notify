package hu.unimiskolc.iit.mobile.carnotify.fragment

import android.app.Activity
import android.app.DatePickerDialog
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import hu.unimiskolc.iit.mobile.carnotify.databinding.CarSettingsBinding
import hu.unimiskolc.iit.mobile.carnotify.getFileName
import hu.unimiskolc.iit.mobile.carnotify.snackbar
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

class CarSettingsFragment: Fragment() {
    private var _binding: CarSettingsBinding? = null

    private val binding get() = _binding!!

    private val viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private lateinit var carDataSource: RoomCarDataSource

    private var selectedImageUri: Uri? = null

    private lateinit var selectedImageFilePath: String

    private lateinit var contentResolver: ContentResolver

    private lateinit var saveDir: File

    private var lastInspectionCalendar: Calendar = Calendar.getInstance()

    private var car: Car? = null

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
        _binding = CarSettingsBinding.inflate(inflater, container, false)

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

        val formatter = SimpleDateFormat("yyyy.MM.dd", Locale.US)

        val carId = arguments?.get("carId") as Int
        val user = arguments?.get("user") as User

        Log.d("test", user.toString())

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

        uiScope.launch {
            car = carDataSource.fetchById(carId)

            val propellant = (binding.propellant.adapter as ArrayAdapter<Propellant>).getPosition(car?.propellant)
            val imageUri = Uri.fromFile(File(car?.image!!))

            binding.type.setText(car?.type)
            binding.licensePlate.setText(car?.licensePlate)
            binding.cylinderCapacity.setText(car?.cylinderCapacity.toString())
            binding.enginePower.setText(car?.enginePower.toString())
            binding.horsepower.setText(car?.horsepower.toString())
            binding.totalMass.setText(car?.totalMass.toString())
            binding.ownMass.setText(car?.ownMass.toString())
            binding.propellant.setSelection(propellant)
            binding.lastInspectionValue.text = formatter.format(car!!.lastInspection)
            lastInspectionCalendar.time = car!!.lastInspection
            selectedImageFilePath = car?.image!!
            binding.uploadImage.setImageURI(imageUri)
        }

        binding.saveButton.setOnClickListener {
            uiScope.launch {
                val type: String = binding.type.text.toString()
                val licensePlate: String = binding.licensePlate.text.toString()
                val cylinderCapacity: Int? = binding.cylinderCapacity.text.toString().toIntOrNull()
                val enginePower: Int? = binding.enginePower.text.toString().toIntOrNull()
                val horsepower: Int? = binding.horsepower.text.toString().toIntOrNull()
                val totalMass: Int? = binding.totalMass.text.toString().toIntOrNull()
                val ownMass: Int? = binding.ownMass.text.toString().toIntOrNull()
                val propellant = binding.propellant.selectedItem as Propellant

                car?.type = if(car?.type != type && type != "") type else car?.type!!
                car?.licensePlate = if(car?.licensePlate != licensePlate && licensePlate != "") licensePlate else car?.licensePlate!!
                car?.cylinderCapacity = if(car?.cylinderCapacity != cylinderCapacity && cylinderCapacity != null) cylinderCapacity else car?.cylinderCapacity!!
                car?.enginePower = if(car?.enginePower != enginePower && enginePower != null) enginePower else car?.enginePower!!
                car?.horsepower = if(car?.horsepower != horsepower && horsepower != null) horsepower else car?.horsepower!!
                car?.totalMass = if(car?.totalMass != totalMass && totalMass != null) totalMass else car?.totalMass!!
                car?.ownMass = if(car?.ownMass != ownMass && ownMass != null) ownMass else car?.ownMass!!
                car?.propellant = if(car?.propellant != propellant) propellant else car?.propellant!!
                car?.lastInspection = lastInspectionCalendar.time
                car?.image = selectedImageFilePath
                car?.owner = user

                carDataSource.update(car!!)

                Toast.makeText(
                    requireContext(),
                    "Your changes has been saved.",
                    Toast.LENGTH_SHORT
                ).show()

                findNavController()
                    .navigate(
                        R.id.action_CarSettingsFragment_to_MyCarsFragment,
                        bundleOf(
                            Pair("user", user)
                        )
                    )
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