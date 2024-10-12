package com.example.divelogbookoffline.add_dive

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.divelogbookoffline.Dive
import com.example.divelogbookoffline.databinding.FragmentAddDiveBinding

class AddDiveFragment : Fragment() {

    private var _binding: FragmentAddDiveBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<AddDiveViewModel> { AddDiveViewModelFactory(requireActivity().application) }


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

        binding.saveButton.setOnClickListener { saveDive() }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun saveDive() {
        if (isDataMissing()) {
            Toast.makeText(context, "Missing input data!", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.saveDive(
                Dive(
                    0,
                    binding.dateInput.text.toString(),
                    binding.diveNameInput.text.toString(),
                    binding.diveSiteInput.text.toString(),
                    binding.bottomTimeInput.text.toString().toInt(),
                    binding.maxDepthInput.text.toString().toInt()
                )
            )
        }
    }

    private fun isDataMissing(): Boolean {
        return (binding.diveNameInput.text.isNullOrBlank()
                || binding.diveSiteInput.text.isNullOrBlank()
                || binding.bottomTimeInput.text.isNullOrBlank()
                || binding.dateInput.text.isNullOrBlank()
                || binding.maxDepthInput.text.isNullOrBlank())
    }
}