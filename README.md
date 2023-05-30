
**Exchange-currency**
---
REST API for describing currencies and exchange rates. Allows you to view and edit lists of currencies and exchange rates, and calculate the conversion of arbitrary amounts from one currency to another.

The web interface for the project is not implied.

Currencies
---

**GET `/currencies`**

get list of currencies:
~~~
[
    {
        "id": 6,
        "name": "Yen",
        "code": "JPY",
        "sign": "¥"
    },
    {
        "id": 7,
        "name": "Won",
        "code": "KRW",
        "sign": "₩"
    },
]
~~~

**GET `/currency/EUR`**

get currency by code:
~~~
{
    "id": 3,
    "name": "Euro",
    "code": "EUR",
    "sign": "€"
}
 ~~~

**POST `/currency`**

Adding a new currency to the database. The data is passed in the request body as form fields (_x-www-form-urlencoded_). Form fields - **name**, **code**, **sign**. An example of a response is a JSON representation of a record inserted into the database:
~~~
{
    "id": 21,
    "name": "Argentine%20Peso",
    "code": "ARS",
    "sign": "Arg%24"
}
~~~

Exchange rates
---
**GET `/exchangeRates`**

Get list of exchange rates:
~~~
[
    {
        "id": 4,
        "baseCurrency": {
            "id": 12,
            "name": "Poland Zloty",
            "code": "PLN",
            "sign": "zł"
        },
        "targetCurrency": {
            "id": 6,
            "name": "Yen",
            "code": "JPY",
            "sign": "¥"
        },
        "rate": 5.0
    }
]
~~~

**GET `/exchangeRates/PLNJPY`**

Get currencies rate by currencies pair codes:
~~~
{
    "id": 4,
    "baseCurrency": {
        "id": 12,
        "name": "Poland Zloty",
        "code": "PLN",
        "sign": "zł"
    },
    "targetCurrency": {
        "id": 6,
        "name": "Yen",
        "code": "JPY",
        "sign": "¥"
    },
    "rate": 5.0
}
~~~

**POST `/exchangeRates`**

Adding a new exchange rate to the database. The data is passed in the request body as form fields (*x-www-form-urlencoded*). Form fields - **baseCurrencyCode**, **targetCurrencyCode**, **rate**. Form fields example:
* **`baseCurrencyCode`** - JPY
* **`targetCurrencyCode`** - EUR
* **`rate`** - 0.0071

An example response is a JSON representation of a record inserted into the database:
~~~
{
    "id": 6,
    "baseCurrency": {
        "id": 6,
        "name": "Yen",
        "code": "JPY",
        "sign": "¥"
    },
    "targetCurrency": {
        "id": 3,
        "name": "Euro",
        "code": "EUR",
        "sign": "€"
    },
    "rate": 0.0071
}
~~~

**PATCH `/exchangeRates/JPYEUR`**

Update of the existing exchange rate in the database. The currency pair is specified by currency codes in the request address. The data is passed in the request body as form fields (*x-www-form-urlencoded*). The only form field is **rate**.

Form fields example:
* **`rate`** - 0.0073


An example response is a JSON representation of an updated record in the database:

~~~
{
    "id": 6,
    "baseCurrency": {
        "id": 6,
        "name": "Yen",
        "code": "JPY",
        "sign": "¥"
    },
    "targetCurrency": {
        "id": 3,
        "name": "Euro",
        "code": "EUR",
        "sign": "€"
    },
    "rate": 0.0073
}
~~~

Exchange
---
**GET `/exchange?from=BASE_CURRENCY_CODE&to=TARGET_CURRENCY_CODE&amount=$AMOUNT`**

Calculation of a certain amount of funds from one currency to another.  
Request example - **GET /exchange?from=USD&to=AUD&amount=5**

~~~
{
    "baseCurrency": {
        "id": 2,
        "name": "UnitedStates of America dollar",
        "code": "USD",
        "sign": "$"
    },
    "targetCurrency": {
        "id": 1,
        "name": "Australian dollar",
        "code": "AUD",
        "sign": "A$"
    },
    "rate": 1.3333333333333333,
    "amount": 5.0,
    "convertedAmount": 6.67
}
~~~
