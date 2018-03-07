Feature: Create user account DTUPay

  Scenario Outline: Trying to create a new DTUPay user account
    Given I want to create a DTUPay user considering my cpr <cpr>
    And my name <name> 
    When DTUPay tries to create me a new account
    Then DTU Pay returns status code <code>
    And I receive the message <reply_message>

    Examples: 
      |			cpr  		  |   name   | code |	     												reply_message												   |
      | "111111-2222" |  "name1" |  400 |  "Please create a bank account before requesting a DTUPay account" |
      | "987654-3210" |  "name2" |	200 |						                                           "987654-3210" |
      | "111111-0000" |  "name3" |  400 |				                         "You already have a DTUPay account" |
