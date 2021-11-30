package hu.unimiskolc.iit.mobile.carnotify

import android.app.Activity
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import hu.unimiskolc.iit.mobile.core.domain.Car
import java.io.File

class MyCarsListAdapter(private val context: Activity, private val cars: List<Car>)
    : ArrayAdapter<Car>(context, R.layout.list_item_car, cars.toTypedArray()) {

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val inflater = context.layoutInflater
        val rowView = inflater.inflate(R.layout.list_item_car, null, true)

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