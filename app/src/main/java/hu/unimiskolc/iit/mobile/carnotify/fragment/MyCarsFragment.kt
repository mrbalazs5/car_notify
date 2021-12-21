package hu.unimiskolc.iit.mobile.carnotify.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavDeepLinkBuilder
import androidx.navigation.fragment.findNavController
import androidx.room.Room
import hu.unimiskolc.iit.mobile.carnotify.MainActivity
import hu.unimiskolc.iit.mobile.carnotify.MyCarsListAdapter
import hu.unimiskolc.iit.mobile.carnotify.R
import hu.unimiskolc.iit.mobile.carnotify.databinding.MyCarsFragmentBinding
import hu.unimiskolc.iit.mobile.core.domain.Car
import hu.unimiskolc.iit.mobile.core.domain.User
import hu.unimiskolc.iit.mobile.framework.db.CarNotifyDatabase
import hu.unimiskolc.iit.mobile.framework.db.datasource.RoomCarDataSource
import hu.unimiskolc.iit.mobile.framework.db.mapper.CarMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*

class MyCarsFragment: Fragment() {
    private var _binding: MyCarsFragmentBinding? = null

    private val binding get() = _binding!!

    private val viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private lateinit var carDataSource: RoomCarDataSource
    private lateinit var user: User

    private val fragment = this

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MyCarsFragmentBinding.inflate(inflater, container, false)

        val db = Room.databaseBuilder(
            this.requireContext(),
            CarNotifyDatabase::class.java, "car_notify.db"
        ).build()

        carDataSource = RoomCarDataSource(db.carDao(), CarMapper())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        user = arguments?.get("user") as User

        binding.userSettingsButton.setOnClickListener {
            findNavController()
                .navigate(
                    R.id.action_MyCarsFragment_to_UserSettingsFragment,
                    bundleOf(Pair("userId", user.id))
                )
        }

        binding.addCarButton.setOnClickListener {
            findNavController()
                .navigate(
                    R.id.action_MyCarsFragment_to_AddCarFragment,
                    bundleOf(Pair("user", user))
                )
        }

        uiScope.launch {
            val cars = carDataSource.fetchByOwner(user)

            binding.myCarsListView.adapter = MyCarsListAdapter(fragment, user, cars)

            checkCarsForNotification(cars)
        }

    }

    private fun checkCarsForNotification(cars: List<Car>) {
        val channelId = getString(R.string.notification_channel_id)
        val builder = NotificationCompat.Builder(fragment.requireContext(), channelId)
            .setSmallIcon(R.mipmap.car_icon)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val lastInspectionCal = Calendar.getInstance()

        val currentCal = Calendar.getInstance()

        val currentYear = currentCal.get(Calendar.YEAR)
        val currentMonth = currentCal.get(Calendar.MONTH)
        val currentDay = currentCal.get(Calendar.DAY_OF_MONTH)

        val userBirthDateCal = Calendar.getInstance()
        userBirthDateCal.time = user.birthDate

        val userBirthYear = userBirthDateCal.get(Calendar.YEAR)
        val userBirthMonth = userBirthDateCal.get(Calendar.MONTH)
        val userBirthDay = userBirthDateCal.get(Calendar.DAY_OF_MONTH)

        if(
            currentYear == userBirthYear &&
            currentMonth == userBirthMonth &&
            currentDay == userBirthDay
        ) {
            builder
                .setContentTitle("Birthday")
                .setContentText("Happy birthday ${user.name}!")

            with(NotificationManagerCompat.from(fragment.requireContext())) {
                notify(0, builder.build())
            }
        }

        if(currentMonth == Calendar.NOVEMBER) {
            builder
                .setContentTitle("Change your tires.")
                .setContentText("It's November! Time to switch to winter tires.")

            with(NotificationManagerCompat.from(fragment.requireContext())) {
                notify(0, builder.build())
            }
        }

        if(currentMonth == Calendar.APRIL) {
            builder
                .setContentTitle("Change your tires.")
                .setContentText("It's March! Time to switch to summer tires.")

            with(NotificationManagerCompat.from(fragment.requireContext())) {
                notify(0, builder.build())
            }
        }

        for(car in cars) {
            lastInspectionCal.time = car.lastInspection
            lastInspectionCal.add(Calendar.MONTH, 3)

            val pendingIntent = NavDeepLinkBuilder(fragment.requireContext())
                .setComponentName(MainActivity::class.java)
                .setGraph(R.navigation.nav_graph)
                .setDestination(R.id.CarFragment)
                .setArguments(bundleOf(Pair("carId", car.id)))
                .createPendingIntent()

            if(currentCal > lastInspectionCal) {
                builder
                    .setContentTitle(car.type + "|" + car.licensePlate)
                    .setContentText("Time to inspect your car in a service.")
                    .setContentIntent(pendingIntent)

                with(NotificationManagerCompat.from(fragment.requireContext())) {
                    notify(car.id, builder.build())
                }
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}