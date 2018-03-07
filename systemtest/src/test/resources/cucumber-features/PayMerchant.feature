Feature: DTUPay customer pays a merchant using a barcode ID

	@pay
  Scenario: A DTUPay customer with a valid barcode makes a transaction to a merchant  
    Given I am a DTUPay customer with a valid barcode
    And a merchant with a bank account
    When I show the merchant my barcode to pay 1000
    And the merchant requests a transaction with comment "commet"
    Then the merchant receives a confirmation      
    
  @pay
  Scenario: A DTUPay customer with an invalid barcode fails a transaction to a merchant  
    Given I am a DTUPay customer with an invalid barcode
    And a merchant with a bank account
    When I show the merchant my barcode to pay 1000
    And the merchant requests a transaction with comment "commet"
    Then the merchant receives an error 404
          
  @pay
  Scenario: A DTUPay customer with a valid barcode and insuffecient funds fails a transaction to a merchant  
    Given I am a DTUPay customer with a valid barcode
    And a merchant with a bank account
    When I show the merchant my barcode to pay more than my funds 
    And the merchant requests a transaction with comment "commet"
    Then the merchant receives an error 500