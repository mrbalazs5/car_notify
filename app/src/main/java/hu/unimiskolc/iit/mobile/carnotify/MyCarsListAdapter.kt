package hu.unimiskolc.iit.mobile.carnotify

import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.fragment.findNavController
import androidx.room.Room
import hu.unimiskolc.iit.mobile.core.domain.Car
import hu.unimiskolc.iit.mobile.core.domain.User
import hu.unimiskolc.iit.mobile.framework.db.CarNotifyDatabase
import hu.unimiskolc.iit.mobile.framework.db.datasource.RoomCarDataSource
import hu.unimiskolc.iit.mobile.framework.db.mapper.CarMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File

class MyCarsListAdapter(private val context: Fragment, private val user: User, private val cars: List<Car>)
    : ArrayAdapter<Car>(context.requireActivity(), R.layout.list_item_car, cars.toTypedArray()) {
    private val viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val inflater = context.layoutInflater
        val rowView = inflater.inflate(R.layout.list_item_car, null, true)

        val db = Room.databaseBuilder(
            context.requireContext(),
            CarNotifyDatabase::class.java, "car_notify.db"
        ).build()

        val carDataSource = RoomCarDataSource(db.carDao(), CarMapper())

        rowView.setOnClickListener {
            context.findNavController()
                .navigate(
                    R.id.action_MyCarsFragment_to_CarFragment,
                    bundleOf(
                        Pair("carId", cars[position].id),
                        Pair("user", user)
                    )
                )
        }

        val removeButton = rowView.findViewById<ImageButton>(R.id.removeCarButton)

        removeButton.setOnClickListener {
            uiScope.launch {
                carDataSource.remove(cars[position])

                val parentFragmentManager = context.parentFragmentManager

                parentFragmentManager
                    .beginTransaction()
                    .detach(context)
                    .commitNow()
                parentFragmentManager
                    .beginTransaction()
                    .attach(context)
                    .commitNow()
            }
        }

        val titleText = rowView.findViewById(R.id.title) as TextView
        val imageView = rowView.findViewById(R.id.icon) as ImageView
        val subtitleText = rowView.findViewById(R.id.description) as TextView

        val imageURI = Uri.fromFile(File(cars[position].image))

        titleText.text = cars[position].type
        imageView.setImageURI(imageURI)
        subtitleText.text = cars[position].licensePlate

        return rowView
    }
}