package com.reznick.spitepine.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.reznick.spitepine.SpitePineApp
import com.reznick.spitepine.data.model.Tree
import com.reznick.spitepine.data.repository.TreeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(repo: TreeRepository) : ViewModel() {
    val trees: StateFlow<List<Tree>> = repo.observeTrees()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    companion object {
        fun factory(app: SpitePineApp) = viewModelFactory {
            initializer { HomeViewModel(app.treeRepository) }
        }
    }
}
