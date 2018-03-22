# Filters in Tablesaw

## General Filter

apply to all types

    equalTo(Comparable c)
    greaterThan(Comparable c)
    greaterThanOrEqualTo(Comparable c)
    lessThan(Comparable c)
    lessThanOrEqualTo(Comparable c)
    missing()
    between(Comparable a, Comparable b)
    in(List aList)
    Logical and Compound Filters
    
    is(Filter filter)
    isNot(Filter filter)
    anyOf(List filters)
    allOf(List filters)
    noneOf(List filters)
    both(Filter a, Filter b)
    either(Filter a, Filter b)
    neither(Filter a, Filter b)
    String Filters
    
    equalToIgnoringCase(String string)
    startsWith(String string)
    endsWith(String string)
    contains(String string)
    matchesRegex(String string)
    isEmpty(String string)
    isAlpha()
    isNumeric()
    isAlphaNumeric()
    isUpperCase()
    isLowerCase()
    hasLengthEqualTo(int lengthChars)
    hasLengthLessThan(int lengthChars)
    hasLengthGreaterThan(int lengthChars)
    Integer Filters
    
    isPositive()
    isNegative()
    isNonNegative()
    isZero()
    isEven()
    isOdd()
    Real Number Filters
    
    isCloseTo(float target);
    isCloseTo(double target)
    isPositive()
    isNegative()
    Date Filters
    
    equalTo(LocalDate date)
    before(LocalDate date)
    after(LocalDate date)
    inYear(int fourDigitYear)
    inQ1()
    inQ2()
    inQ3()
    inQ4()
    inJanuary(), inFebruary(), …, inDecember()
    sunday(), monday(), …, saturday()
    firstDayOfMonth()
    lastDayOfMonth()
    Time Filters
    
    midnight()
    AM()
    PM()


## DateTime Filter


