package com.example.bankingapp.data.remote

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

class MockInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()

        // Simulate network delay
        Thread.sleep(if (com.bankingapp.secure.BuildConfig.DEBUG) 1000 else 500)

        val responseJson = when {
            url.contains("/account/balance") -> getMockAccountBalance()
            url.contains("/account/details") -> getMockAccountDetails()
            url.contains("/transactions") -> getMockTransactions()
            url.contains("/cards") -> getMockCards()
            else -> """{"error": "Endpoint not found"}"""
        }

        return Response.Builder()
            .code(200)
            .message("OK")
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .body(responseJson.toResponseBody("application/json".toMediaType()))
            .build()
    }

    private fun getMockAccountBalance(): String = """
        {
            "id": "acc_12345",
            "accountNumber": "1234567890",
            "accountType": "CHECKING",
            "balance": {
                "amount": "2547.83",
                "currency": "USD"
            },
            "currency": "USD",
            "isActive": true,
            "lastUpdated": "2024-03-15T10:30:00Z",
            "createdDate": "2023-01-15T09:00:00Z"
        }
    """

    private fun getMockAccountDetails(): String = """
        {
            "id": "acc_12345",
            "accountNumber": "1234567890",
            "accountType": "CHECKING",
            "balance": {
                "amount": "2547.83",
                "currency": "USD"
            },
            "currency": "USD",
            "isActive": true,
            "lastUpdated": "2024-03-15T10:30:00Z",
            "createdDate": "2023-01-15T09:00:00Z"
        }
    """

    private fun getMockTransactions(): String = """
        [
            {
                "id": "txn_001",
                "accountId": "acc_12345",
                "amount": {
                    "amount": "-85.00",
                    "currency": "USD"
                },
                "type": "ONLINE_PURCHASE",
                "status": "COMPLETED",
                "description": "Amazon Purchase",
                "recipientName": "Amazon.com",
                "recipientAccount": null,
                "reference": "AMZ-2024-001",
                "date": "2024-03-15T09:15:00Z",
                "balanceAfter": {
                    "amount": "2547.83",
                    "currency": "USD"
                }
            },
            {
                "id": "txn_002",
                "accountId": "acc_12345",
                "amount": {
                    "amount": "-45.50",
                    "currency": "USD"
                },
                "type": "ATM_WITHDRAWAL",
                "status": "COMPLETED",
                "description": "ATM Withdrawal",
                "recipientName": null,
                "recipientAccount": null,
                "reference": "ATM-2024-002",
                "date": "2024-03-14T14:30:00Z",
                "balanceAfter": {
                    "amount": "2632.83",
                    "currency": "USD"
                }
            },
            {
                "id": "txn_003",
                "accountId": "acc_12345",
                "amount": {
                    "amount": "1200.00",
                    "currency": "USD"
                },
                "type": "DEPOSIT",
                "status": "COMPLETED",
                "description": "Salary Deposit",
                "recipientName": null,
                "recipientAccount": null,
                "reference": "SAL-2024-003",
                "date": "2024-03-13T08:00:00Z",
                "balanceAfter": {
                    "amount": "2678.33",
                    "currency": "USD"
                }
            },
            {
                "id": "txn_004",
                "accountId": "acc_12345",
                "amount": {
                    "amount": "-25.99",
                    "currency": "USD"
                },
                "type": "PAYMENT",
                "status": "COMPLETED",
                "description": "Netflix Subscription",
                "recipientName": "Netflix Inc.",
                "recipientAccount": null,
                "reference": "NFLX-2024-004",
                "date": "2024-03-12T12:00:00Z",
                "balanceAfter": {
                    "amount": "1478.33",
                    "currency": "USD"
                }
            },
            {
                "id": "txn_005",
                "accountId": "acc_12345",
                "amount": {
                    "amount": "-156.78",
                    "currency": "USD"
                },
                "type": "ONLINE_PURCHASE",
                "status": "COMPLETED",
                "description": "Grocery Shopping",
                "recipientName": "Whole Foods Market",
                "recipientAccount": null,
                "reference": "WFM-2024-005",
                "date": "2024-03-11T16:45:00Z",
                "balanceAfter": {
                    "amount": "1504.32",
                    "currency": "USD"
                }
            }
        ]
    """

    private fun getMockCards(): String = """
        [
            {
                "id": "card_001",
                "accountId": "acc_12345",
                "cardNumber": "4532123456789012",
                "maskedNumber": "**** **** **** 9012",
                "holderName": "JOHN DOE",
                "expiryMonth": 12,
                "expiryYear": 2027,
                "cvv": "123",
                "cardType": "DEBIT",
                "brand": "VISA",
                "isActive": true,
                "isBlocked": false,
                "dailyLimit": {
                    "amount": "1000.00",
                    "currency": "USD"
                },
                "monthlyLimit": {
                    "amount": "5000.00",
                    "currency": "USD"
                },
                "lastUsed": "2024-03-15T09:15:00Z",
                "createdDate": "2023-01-15T09:00:00Z"
            },
            {
                "id": "card_002",
                "accountId": "acc_12345",
                "cardNumber": "5555444433332222",
                "maskedNumber": "**** **** **** 2222",
                "holderName": "JOHN DOE",
                "expiryMonth": 8,
                "expiryYear": 2026,
                "cvv": "456",
                "cardType": "CREDIT",
                "brand": "MASTERCARD",
                "isActive": true,
                "isBlocked": false,
                "dailyLimit": {
                    "amount": "2000.00",
                    "currency": "USD"
                },
                "monthlyLimit": {
                    "amount": "10000.00",
                    "currency": "USD"
                },
                "lastUsed": "2024-03-12T14:30:00Z",
                "createdDate": "2023-06-20T10:30:00Z"
            }
        ]
    """
}