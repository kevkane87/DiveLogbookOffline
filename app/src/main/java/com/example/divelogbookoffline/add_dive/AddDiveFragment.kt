package com.example.divelogbookoffline.add_dive

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.divelogbookoffline.Dive
import com.example.divelogbookoffline.R
import com.example.divelogbookoffline.databinding.FragmentAddDiveBinding

class AddDiveFragment : Fragment() {

    private var _binding: FragmentAddDiveBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<AddDiveViewModel> { AddDiveViewModelFactory(requireActivity().application) }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                UserInputScreen { diveTitle, diveSite, date, bottomTime, maxDepth ->
                    Log.d("UserInput", "diveTitle: $diveTitle, diveSite: $diveSite, date: $date")
                    viewModel.saveDive(Dive(
                        0,
                        date = date, diveTitle = diveTitle, diveSite = diveSite, bottomTime = bottomTime.toInt(), maxDepth = maxDepth.toDouble()

                    ))
                }
            }
        }
    }


    @Composable
    fun UserInputScreen(onSave: (String, String, String, String, String) -> Unit) {

        var diveName by remember { mutableStateOf("") }
        var diveSite by remember { mutableStateOf("") }
        var date by remember { mutableStateOf("") }
        var bottomTime by remember { mutableStateOf("") }
        var maxDepth by remember { mutableStateOf("") }

        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = stringResource(id = R.string.add_dive), fontSize = 24.sp, modifier = Modifier.padding(bottom = 16.dp))

            TextField(
                value = diveName,
                onValueChange = { diveName = it },
                label = { Text(stringResource(id = R.string.dive_name)) },
                modifier = Modifier.fillMaxWidth()
            )


            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = diveSite,
                onValueChange = { diveSite = it },
                label = { Text(stringResource(id = R.string.dive_site)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))


            TextField(
                value = date,
                onValueChange = { date = it },
                label = { Text(stringResource(id = R.string.date)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = bottomTime,
                onValueChange = { bottomTime = it },
                label = { Text(stringResource(id = R.string.bottom_time_mins)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = maxDepth,
                onValueChange = { maxDepth = it },
                label = { Text(stringResource(id = R.string.max_depth_m)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))


            Button(
                onClick = {
                    onSave(diveName, diveSite, date, bottomTime, maxDepth )
                    diveName = ""
                    diveSite = ""
                    date = ""
                    bottomTime = ""
                    maxDepth = ""
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonColors(Color.Blue, Color.Blue, Color.Red, Color.Red)
            ) {
                Text(stringResource(id = R.string.save), color = Color.Yellow)
            }
        }
    }

   /* override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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
                    binding.maxDepthInput.text.toString().toDouble()
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
    }*/
}