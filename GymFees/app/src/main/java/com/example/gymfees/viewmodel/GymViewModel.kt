package com.example.gymfees.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.gymfees.data.Customer
import com.example.gymfees.data.GymDatabase
import com.example.gymfees.data.Payment
import com.example.gymfees.repository.GymRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class GymViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GymRepository
    
    private val _searchQuery = MutableLiveData("")
    private val _filterStatus = MutableLiveData("ALL") // ALL, PAID, PENDING, DUE_SOON, OVERDUE

    val filteredCustomers: MediatorLiveData<List<Customer>> = MediatorLiveData()
    val customerCount: LiveData<Int>
    val paidCount: LiveData<Int>
    val dueSoonCount: LiveData<Int>
    val overdueCount: LiveData<Int>
    val totalCollection: LiveData<Double?>
    val monthCollection: LiveData<Double?>

    // For Snackbar messages
    private val _snackbarMessage = MutableLiveData<String?>()
    val snackbarMessage: LiveData<String?> get() = _snackbarMessage

    // For undo functionality
    private val _lastAction = MutableLiveData<LastAction?>()
    val lastAction: LiveData<LastAction?> get() = _lastAction

    sealed class LastAction {
        data class MarkPaid(val customerBeforeAction: Customer, val newPayment: Payment) : LastAction()
        data class MarkUnpaid(val customerBeforeAction: Customer, val deletedPayment: Payment) : LastAction()
    }

    init {
        val database = GymDatabase.getDatabase(application)
        repository = GymRepository(database)
        
        val today = Calendar.getInstance().apply { 
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0) 
        }.timeInMillis
        
        // Start of current month for collection calculation
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfMonth = calendar.timeInMillis

        customerCount = repository.customerCount
        paidCount = repository.paidCustomerCount
        dueSoonCount = repository.getDueSoonCount(today)
        overdueCount = repository.getOverdueCount(today)
        monthCollection = repository.getTotalCollectionForMonth(startOfMonth)
        totalCollection = repository.getTotalCollection()

        filteredCustomers.addSource(repository.allCustomers) { customers ->
            combine(customers, _searchQuery.value ?: "", _filterStatus.value ?: "ALL")
        }
        filteredCustomers.addSource(_searchQuery) { query ->
            combine(repository.allCustomers.value ?: emptyList(), query, _filterStatus.value ?: "ALL")
        }
        filteredCustomers.addSource(_filterStatus) { status ->
            combine(repository.allCustomers.value ?: emptyList(), _searchQuery.value ?: "", status)
        }
    }

    private fun combine(customers: List<Customer>, query: String, status: String) {
        filteredCustomers.value = filterAndSearch(customers, query, status)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterStatus(status: String) {
        _filterStatus.value = status
    }

    private fun calculateOverallStatus(nextDueDate: Long, today: Long): String {
        val threeDaysMillis = 3 * 24 * 60 * 60 * 1000L
        return when {
            nextDueDate < today -> "OVERDUE"
            nextDueDate <= today + threeDaysMillis -> "DUE_SOON"
            else -> "PENDING"
        }
    }

    private fun filterAndSearch(customers: List<Customer>, query: String, status: String): List<Customer> {
        val today = Calendar.getInstance().apply { 
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0) 
        }.timeInMillis

        return customers.filter { customer ->
            val matchesSearch = customer.name.contains(query, ignoreCase = true) || 
                               customer.mobileNumber.contains(query)
            
            val matchesFilter = when (status) {
                "ALL" -> true
                "PAID" -> customer.isCurrentMonthFeePaid
                "PENDING" -> !customer.isCurrentMonthFeePaid && calculateOverallStatus(customer.nextDueDate, today) == "PENDING"
                "DUE_SOON" -> !customer.isCurrentMonthFeePaid && calculateOverallStatus(customer.nextDueDate, today) == "DUE_SOON"
                "OVERDUE" -> !customer.isCurrentMonthFeePaid && calculateOverallStatus(customer.nextDueDate, today) == "OVERDUE"
                else -> true
            }
            matchesSearch && matchesFilter
        }
    }

    fun insertCustomer(name: String, mobile: String, joiningDate: Long, fee: Double, notes: String?) = viewModelScope.launch(Dispatchers.IO) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = joiningDate
        calendar.add(Calendar.MONTH, 1)
        val nextDueDate = calendar.timeInMillis
        
        val customer = Customer(
            name = name,
            mobileNumber = mobile,
            joiningDate = joiningDate,
            monthlyFee = fee,
            nextDueDate = nextDueDate,
            notes = notes,
            isCurrentMonthFeePaid = false,
            overallStatus = "PENDING"
        )
        repository.insertCustomer(customer)
    }

    suspend fun getCustomerById(id: Long): Customer? = withContext(Dispatchers.IO) {
        repository.getCustomerById(id)
    }

    fun getCustomerByIdLiveData(id: Long): LiveData<Customer?> {
        return repository.getCustomerByIdLiveData(id)
    }

    fun updateCustomer(customer: Customer) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateCustomer(customer)
    }

    fun deleteCustomer(customer: Customer) = viewModelScope.launch(Dispatchers.IO) {
        repository.deletePaymentsByCustomerId(customer.id)
        repository.deleteCustomer(customer)
    }

    fun markCustomerPaid(customer: Customer) = viewModelScope.launch(Dispatchers.IO) {
        if (customer.isCurrentMonthFeePaid) return@launch

        val customerBeforeAction = customer.copy()

        val paymentDate = System.currentTimeMillis()
        val payment = Payment(
            customerId = customer.id,
            customerName = customer.name,
            amountPaid = customer.monthlyFee,
            paymentDate = paymentDate,
            dueDate = customer.nextDueDate
        )

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = customer.nextDueDate
        calendar.add(Calendar.MONTH, 1)
        val newNextDueDate = calendar.timeInMillis

        val updatedCustomer = customer.copy(
            nextDueDate = newNextDueDate,
            isCurrentMonthFeePaid = true,
            overallStatus = "PAID"
        )
        
        repository.markPaidTransaction(customer, payment, updatedCustomer)
        _lastAction.postValue(LastAction.MarkPaid(customerBeforeAction, payment))
    }

    fun markCustomerUnpaid(customer: Customer) = viewModelScope.launch(Dispatchers.IO) {
        if (!customer.isCurrentMonthFeePaid) return@launch
        
        val customerBeforeAction = customer.copy()
        val latestPayment = repository.getLatestPaymentForCustomer(customer.id)

        if (latestPayment == null) {
            _snackbarMessage.postValue("No payment record found to revert.")
            return@launch
        }

        val previousNextDueDate = latestPayment.dueDate
        val updatedCustomer = customer.copy(
            nextDueDate = previousNextDueDate,
            isCurrentMonthFeePaid = false,
            overallStatus = calculateOverallStatus(previousNextDueDate, System.currentTimeMillis())
        )
        
        repository.markUnpaidTransaction(latestPayment, updatedCustomer)
        _lastAction.postValue(LastAction.MarkUnpaid(customerBeforeAction, latestPayment))
    }

    fun undoLastAction() = viewModelScope.launch(Dispatchers.IO) {
        val actionToUndo = _lastAction.value
        _lastAction.postValue(null)

        when (actionToUndo) {
            is LastAction.MarkPaid -> {
                repository.undoActionTransaction(
                    customerToRevert = actionToUndo.customerBeforeAction,
                    paymentToDelete = actionToUndo.newPayment,
                    paymentToRestore = null
                )
                _snackbarMessage.postValue("Payment undone.")
            }
            is LastAction.MarkUnpaid -> {
                repository.undoActionTransaction(
                    customerToRevert = actionToUndo.customerBeforeAction,
                    paymentToDelete = null,
                    paymentToRestore = actionToUndo.deletedPayment
                )
                _snackbarMessage.postValue("Reversal undone.")
            }
            null -> {}
        }
    }

    fun getPaymentsForCustomer(customerId: Long): LiveData<List<Payment>> {
        return repository.getPaymentsForCustomer(customerId)
    }

    fun snackbarMessageShown() {
        _snackbarMessage.value = null
    }

    fun clearLastAction() {
        _lastAction.value = null
    }
}
