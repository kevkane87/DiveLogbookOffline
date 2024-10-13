package com.example.divelogbookoffline.list_dives

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.fragment.findNavController
import com.example.divelogbookoffline.Dive
import com.example.divelogbookoffline.R
import com.example.divelogbookoffline.databinding.FragmentListDivesBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ListDivesFragment: Fragment() {

    private var _binding: FragmentListDivesBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<ListDivesViewModel> { ListDivesViewModelFactory(requireActivity().application) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                    ItemListScreen()
                }
            }
        }

    @Preview(showBackground = true)
    @Composable
    fun DiveListPreview() {
        val sampleDives = listOf("Dive 1", "Dive 2", "Dive 3", "Dive 4", "Dive 5")
        ItemListScreen()
    }


    @Composable
    fun ItemListScreen() {

        val factory = remember { ListDivesViewModelFactory(requireActivity().application) }
        val viewModel: ListDivesViewModel = viewModel(factory = factory)
        val dives by viewModel.dives.collectAsState()

        LazyColumn {
            items(dives) { dive ->
                ItemView(dive) {
                    Log.d("LIST DIVES", "Clicked: ${dive.diveTitle}")
                }
            }
        }
    }

    @Composable
    fun ItemView(item: Dive, onClick: (Dive) -> Unit) {
        Card(modifier = Modifier.clickable { onClick(item) }
            .fillMaxWidth()
            .padding(8.dp, 8.dp, 8.dp)) {
           Text(
           text = item.diveTitle?:"",
               fontSize = 16.sp,
           modifier = Modifier
               .padding(16.dp, 4.dp)

           )

           Text(
               text = item.diveSite?:"",
               fontSize = 14.sp,
               modifier = Modifier
                   .padding(16.dp, 4.dp)
           )

           Text(
               text = item.date?:"",
               fontSize = 12.sp,
               modifier = Modifier
                   .padding(16.dp, 4.dp)
           )

            Text(
                text = item.bottomTime.toString(),
                fontSize = 12.sp,
                modifier = Modifier
                    .padding(16.dp, 4.dp)
            )

            Text(
                text = item.maxDepth.toString(),
                fontSize = 12.sp,
                modifier = Modifier
                    .padding(16.dp, 4.dp)
            )
       }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val menuHost: MenuHost = requireActivity()

        /*val adapter = ListDivesAdapter()
        binding.listDivesRecyclerview.adapter = adapter

        // Collect from the Flow in the ViewModel, and submit it to the adapter
        lifecycleScope.launch {
            viewModel.allDives().collectLatest {
                adapter.submitList(it)
            }
        }*/

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                return when (menuItem.itemId) {
                    R.id.action_add -> {
                        findNavController().navigate(R.id.action_listDivesFragment_to_addDiveFragment)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
}