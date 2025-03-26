# Review: TICKET-101 - Implement MVP scope of decision engine

## Summary
This review validates the implementation provided in TICKET-101 and outlines what was done well and what areas 
required improvement. The goal of the task was to implement a decision engine that determines the maximum loan amount 
for a customer based on a credit scoring formula, with fallback logic in case the requested loan cannot be approved.

---

## What Was Done Well
- The project structure was organized clearly, with a separation of backend and frontend.
- Frontend-backend communication worked as expected, and the UI was intuitive.
- Spring for the most part was configured correctly using appropriate annotations.
- The application could run end-to-end and returned loan decisions.
- Encapsulation principles were followed by using appropriate access modifiers.
- Custom exceptions were created with easy-to-understand names.

---

## Issues Identified and Resolved

### 1. Simplified loan calculation algorithm
The original decision engine used a basic calculation: creditModifier * loanPeriod. This skipped the requirement to
use the specified scoring formula: ((creditModifier / loanAmount) * loanPeriod) / 10. While this simplified logic
worked well with the default credit score threshold of 0.1, it started producing incorrect results when the threshold
was increased. The simplified approach may have been an attempt at optimization, but it overlooked the importance of
scalability. Although the assignment does not explicitly mention the need to change the threshold, I decided to
implement the full formula and extract the threshold into a constant. This way, it can be easily adjusted in the
future if needed.

### 2. Questionable segmentation logic for credit modifier
The original implementation derived the customer segment from the last four digits of the personal code. While the 
original logic may have been acceptable, I decided to adjust it so that the example personal codes from the assignment 
would behave as described. In my implementation, the credit modifier is assigned based on the last digit of the 
personal code, which ensures that the test cases match the expected segments.

### 3. Inconsistent maximum loan period
The constant for the maximum loan period was incorrectly set to 60 in multiple places (constants, frontend slider, 
and documentation). I updated all instances to reflect the correct value of 48 months.

### 4. Issues in frontend logic
The frontend logic overwrote backend-calculated values with user input. I corrected this logic to preserve the backend
decision. As mentioned above, the frontend also used an incorrect max value (60 months) for the loan period slider.

### 5. Improper use of DTO as a Spring bean
In the original implementation, DecisionResponse was annotated with @Component and injected into the controller as
a singleton, introducing potential race conditions and violating stateless design principles. I removed the annotation
and now create response objects per request to ensure statelessness.

### 6. Use of class-level field for creditModifier
The intern's code stored creditModifier as a class-level field, which introduced potential side effects and 
threading issues. I refactored it into a method-local variable.

### 7. Manual try/catch blocks and Throwable exceptions
Error handling was implemented manually in the controller and service using try/catch blocks. Additionally, all 
custom exceptions extended Throwable, which is not considered good practice. I updated them to extend RuntimeException, 
removed the try/catch blocks, and delegated all exception handling to a centralized GlobalExceptionHandler class.

### 8. Redundant Decision class
A separate Decision class was defined, but its structure was identical to DecisionResponse — even the comments were 
the same. While I understand that in a real banking environment it might be important to follow a layered architecture, 
I decided that in the context of this test assignment it was reasonable to simplify the design. I removed the Decision 
class and used DecisionResponse directly instead.

### 9. Potential risk when sending null values
The original DecisionRequest used primitive type — int, which could lead to errors when a request was sent with null 
value. I updated all fields in both DecisionRequest and DecisionResponse to use wrapper type Integer. Null checks are
now performed in the service layer, and any thrown exceptions are handled by the GlobalExceptionHandler. I also 
converted both DTOs into Java record classes.

---

## Most Critical Issue
It's not easy to name a single most critical issue, as both the loan calculation logic and the incorrect loan period 
limit had notable impact. The simplified formula used in the original implementation worked under the default threshold 
and may be acceptable within the assignment scope. However, it lacked flexibility and failed under different scoring 
rules. At the same time, the incorrect MAXIMUM_LOAN_PERIOD value (60 instead of 48) clearly violated the specification 
and affected both backend logic and frontend behavior. I’d be happy to receive feedback and learn what the 
intended answer was!
