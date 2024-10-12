package com.example.divelogbookoffline.list_dives

import android.os.Bundle
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
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
    ): View? {
        _binding = FragmentListDivesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val menuHost: MenuHost = requireActivity()

        val adapter = ListDivesAdapter()
        binding.listDivesRecyclerview.adapter = adapter

        // Collect from the Flow in the ViewModel, and submit it to the adapter
        lifecycleScope.launch {
            viewModel.allDives().collectLatest {
                adapter.submitList(it)
            }
        }

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