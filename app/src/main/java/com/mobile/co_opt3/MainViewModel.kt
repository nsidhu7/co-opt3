package com.mobile.co_opt3

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update


// Annotating with FlowPreview
@OptIn(FlowPreview::class)
class MainViewModel: ViewModel() {

    // MutableStateFlow to hold the search text
    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    // MutableStateFlow to track whether a search is in progress
    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    // MutableStateFlow to hold the list of persons
    private val _persons = MutableStateFlow(allPersons)

    // Flow that combines search text and persons, applying filtering logic
    val persons = searchText
        .debounce(1000L)// Debounce flow operator to wait for user to stop typing
        .onEach { _isSearching.update { true } }// Set isSearching to true when search starts
        .combine(_persons) { text, persons ->
            if(text.isBlank()) {
                persons// If search text is blank, return all persons
            } else {
                delay(2000L) // Simulate a delay (e.g., network request)
                persons.filter {
                    it.doesMatchSearchQuery(text)
                }
            }
        }
        .onEach { _isSearching.update { false } } // Set isSearching to false when search completes
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000), // Keep the state for 5 seconds after subscribers disconnect
            _persons.value
        )

    // Function to handle changes in search text
    fun onSearchTextChange(text: String) {
        _searchText.value = text
    }
}

// Data class representing a Person
data class Person(
    val firstName: String,
    val lastName: String
) {
    // Function to check if the person matches the search query
    fun doesMatchSearchQuery(query: String): Boolean {
        val matchingCombinations = listOf(
            "$firstName$lastName",
            "$firstName $lastName",
            "${firstName.first()} ${lastName.first()}",
        )

        return matchingCombinations.any {
            it.contains(query, ignoreCase = true)
        }
    }
}

// List of sample persons
private val allPersons = listOf(
    Person(
        firstName = "Noordeep",
        lastName = "Sidhu"
    ),
    Person(
        firstName = "Ryan",
        lastName = "Winthrop"
    ),
    Person(
        firstName = "Garvit",
        lastName = "Madaan"
    ),
    Person(
        firstName = "John",
        lastName = "Smith"
    ),
    Person(
        firstName = "Rick",
        lastName = "Novak"
    ),
    Person(
        firstName = "Jeff",
        lastName = "Johnson"
    ),
    Person(
        firstName = "Susan",
        lastName = "Roy"
    )
)