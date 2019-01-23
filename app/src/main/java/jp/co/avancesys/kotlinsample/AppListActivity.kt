package jp.co.avancesys.kotlinsample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.Toast
import jp.co.avancesys.kotlinsample.recycleView.RecycleViewHolder
import jp.co.avancesys.kotlinsample.recycleView.RecyclerAdapter
import kotlinx.android.synthetic.main.activity_app_list.*

class AppListActivity : AppCompatActivity(), RecycleViewHolder.ItemClickListener {

    companion object {
        private val TAG = AppListActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_list)

        val applications = packageManager.getInstalledApplications(0).map {
            it.loadLabel(packageManager).toString()
        }

        recyclerViewAppList.adapter = RecyclerAdapter(this, this, applications)
        recyclerViewAppList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        title = "AppList"
    }

    override fun onItemClick(view: View, position: Int, name: String) {
        Toast.makeText(this, "$name position $position was tapped", Toast.LENGTH_SHORT).show()
    }
}
