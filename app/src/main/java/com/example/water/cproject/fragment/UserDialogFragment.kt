package com.example.water.cproject.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import com.example.water.cproject.DBResolver
import com.example.water.cproject.MainActivity
import com.example.water.cproject.R

/**
 * Created by watering on 18. 3. 23.
 */

class UserDialogFragment : DialogFragment() {
    private var mainActivity: MainActivity? = null
    private var builder: AlertDialog.Builder? = null
    private var resolver: DBResolver? = null
    private var listener: UserListener? = null
    private var listsCode: List<String>? = null
    private var position: Int = 0

    interface UserListener {
        fun onWorkComplete()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        mainActivity = activity as MainActivity
        builder = AlertDialog.Builder(mainActivity)

        this.resolver = mainActivity!!.resolver
        listsCode = resolver!!.getCodes(mainActivity!!.today)
        builder!!.setTitle(R.string.delete_code)
        builder!!.setPositiveButton("삭제") { dialog, which ->
            val code = listsCode!![position]
            resolver!!.deleteInfoMachine(code)
            resolver!!.deleteCode(code)
            listener!!.onWorkComplete()
        }
        builder!!.setNegativeButton("취소") { dialog, which -> }

        return builder!!.create()
    }

    companion object {
        fun newInstance(position: Int, listener: UserListener): UserDialogFragment {
            val fragment = UserDialogFragment()
            fragment.position = position
            fragment.listener = listener

            return fragment
        }
    }
}
