# matchbox

## Purpose

Add something like pattern matching to Clojure. This attempt is very simplistic yet quite powerful, very extensible and easy to understand.

## How it works

There's a multimethod called 'match?' which takes two arguments (pattern and testee) and dispatches on the types of those arguments. It returns a true value
if testee matches the pattern and a false value if it doesn't. A couple of implementations for 'match?' are included but you can add your own if you want to.

## match? examples

First argument is the pattern, second argument the expression to be tested against the pattern.

Matching on type:

	(match? java.lang.Integer 5)
	=> true

Matching sequences:

	(match? [1 2 _] [1 2 3])
	=> true

_ is a wildcard for exactly one arbitrary item. It also works outside of sequences:

	(match? _ 123)
	=> true

_* is a wildcard for zero or more arbitrary items in a sequence:

	(match? [1 2 _*] [1 2 3 4 5])
	=> true

Using predicates as patterns:

	(match? even? 4)
	=> true

Everything nests:

	(match? [_ even? _*] [1 2 3])
	=> true

	(match? [1 [_ _] 4] [1 [2 3] 4])
	=> true

Matching maps:

	(match? {:a 1 :b even? c: _}
			{:a 1 :b 2 :c 3})
	=> true

	(match? {:a [_ 2 _*] :b "4"}
			{:a [1 2 3] :b "4"})
	=> true

Assuming 'persons' is a sequence of maps containing person data this will return a sequence
of persons called 'Smith' living in New York on Liberty Street:

	(filter
	  (partial match? {:last-name "Smith"
					   :address {:city "New York"
								 :street "Liberty Street"}})
	  persons)

Matching also works on regular expressions (returns first match or nil):

	(match? #"123" "1234")
	=> "123"
	
If there's no specific matching defined for the type of the pattern passed in, pattern and testee are simply
compared for equality:

	(match? 123 123)
	=> true
	
Matching intervals (inclusive upper/lower bounds):

	(match? [-5 5] 3)
	=> true

## "Pattern matching"

One can use 'condp' to do pattern matching using 'match?':

	(condp match? 5
	  even? "even number"
	  _ "not even")
	=> "not even"

There's a macro that does just that:

	(pattern-match 5
	  (even? :> "even number")
	  (_ :> "not even"))

The expression (here: 5) is matched against the given patterns. The body (arbitrary number of forms after :>) of
the first match is evaluated and returned. It's also possible to add an "intermediate step" to destructure the
expression:

	(pattern-match [1 2 3 4 5]
	  ([_ _] :> "list of two items")
	  ([_ _ _ _*] :> [a b c & more] :> (str "list of three items (" a " " b " " c ") and more: " more)))

	(defn my-map
	  [f lst]
	  (pattern-match lst
		([] :> '())
		([_ _*] :> [x & xs] :> (cons (f x) (my-map f xs)))))
		
	(my-map inc [1 2 3])
	=> (2 3 4)