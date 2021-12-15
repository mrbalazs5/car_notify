package hu.unimiskolc.iit.mobile.carnotify

import android.app.Activity
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import hu.unimiskolc.iit.mobile.core.domain.Car
import java.io.File

class MyCarsListAdapter(private val context: Fragment, private val cars: List<Car>)
    : ArrayAdapter<Car>(context.requireActivity(), R.layout.list_item_car, cars.toTypedArray()) {

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val inflater = context.layoutInflater
        val rowView = inflater.inflate(R.layout.list_item_car, null, true)

        rowView.setOnClickListener {
            context.findNavController()
                .navigate(
                    R.id.action_MyCarsFragment_to_CarFragment,
                    bundleOf(Pair("carId", cars[position].id))
                )
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