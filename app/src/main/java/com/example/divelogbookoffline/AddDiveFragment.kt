package com.example.divelogbookoffline

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.divelogbookoffline.databinding.FragmentAddDiveBinding

class AddDiveFragment  : Fragment()  {

    private var _binding: FragmentAddDiveBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddDiveBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.saveButton.setOnClickListener { saveDive()}

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun saveDive(){
        if (isDataMissing()){
            Toast.makeText(context, "Missing input data!", Toast.LENGTH_SHORT).show()
        }
        else{


        }
    }

    private fun isDataMissing () :Boolean  {
        return (binding.diveNameInput.text.isNullOrBlank()
                || binding.bottomTimeInput.text.isNullOrBlank()
                || binding.dateInput.text.isNullOrBlank()
                || binding.maxDepthInput.text.isNullOrBlank())
    }
}