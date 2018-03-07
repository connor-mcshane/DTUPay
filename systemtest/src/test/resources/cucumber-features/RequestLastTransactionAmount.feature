Feature: Get the amount of the last transaction

	@amount
  Scenario Outline: Trying to get the amount of the last transaction
    Given I want to get the amount of my last transaction considering my <cpr>
    When initializing the request to DTUPay
    Then DTUPay returns the response <response>
    
    Examples:
    |			cpr			 |						 response						|
    | "926489-4368" | 						"20"								|
    | "678343-4389" |"You don't have any transaction"	|
