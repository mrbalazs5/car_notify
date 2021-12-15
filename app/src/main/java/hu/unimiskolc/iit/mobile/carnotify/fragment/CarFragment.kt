package hu.unimiskolc.iit.mobile.carnotify.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.room.Room
import hu.unimiskolc.iit.mobile.carnotify.MyCarsListAdapter
import hu.unimiskolc.iit.mobile.carnotify.databinding.CarFragmentBinding
import hu.unimiskolc.iit.mobile.carnotify.databinding.MyCarsFragmentBinding
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
import java.text.SimpleDateFormat
import java.util.*

class CarFragment: Fragment() {
    private var _binding: CarFragmentBinding? = null

    private val binding get() = _binding!!

    private val viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private lateinit var carDataSource: RoomCarDataSource

    private val context = this

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CarFragmentBinding.inflate(inflater, container, false)

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

        uiScope.launch {
            val car = carDataSource.fetchById(carId)

            val imageURI = Uri.fromFile(File(car!!.image))

            binding.image.setImageURI(imageURI)
            binding.type.text = car.type
            binding.lastInspection.text = formatter.format(car.lastInspection)
            binding.licensePlate.text = car.licensePlate
            binding.cylinderCapacity.text = car.cylinderCapacity.toString()
            binding.enginePower.text = car.enginePower.toString()
            binding.horsepower.text = car.horsepower.toString()
            binding.totalMass.text = car.totalMass.toString()
            binding.ownMass.text = car.ownMass.toString()
            binding.propellant.text = car.propellant.name.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.getDefault()
                ) else it.toString()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}