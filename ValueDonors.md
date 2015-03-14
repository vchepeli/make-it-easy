Under the hood, A Maker uses "donors" to get the values of the properties of the object it is making. A "donor", is an object that gives a value (or values) to its caller.

When we use a `with(property, value)` clause to define the value of a property for a Maker, the second (value) argument can be a donor. That donor that is called every time the Maker creates a new instance to provide the property value for that instance.

## Same Value Donor ##

The simplest donor is the `SameValueDonor<T>`, which always returns the value that was passed to its constructor.  You will probably not use this type directly, but call overloads of the `with(...)` API call that take values as parameters or use the `theSame` API call to control sharing (see MakerClausesAndSharing).

```
Maker<Order> anOrder = an(Order, with(customer, theSame(Customer, with(name, "Alice")))));
```

## Makers are Donors ##

A Maker is itself a donor that returns a new object instance every time.  For example, the a(Customer) clause below returns a Maker, which is then used as a donor of Customers for the Order.customer property.  The result is that every Order created by `m` refers to a different customer instance.

```
Maker<Order> m = an(Order, with(customer, a(Customer)));
```


## Custom Donors ##

We can write new types of donor to define property values in different ways.  A donor is just an object that implements the Donor interface:

```
public interface Donor<T> {
    T value();
}
```

For example, sometimes we need the value of some property to be different for each instance.  We can write a Donor to do that.  For example, the Donor below returns universally unique ID strings.

```
class UUIDValue implements Donor<String> {
    @Override
    public String value() {
        return UUID.randomUUID().toString();
    }
}

Maker<NamedThing> aUniquelyNamedThing = a(NamedThing, with(name, new UUIDValue()));
```


## Sequences ##

Sometimes we want values to be allocated from a sequence, so we can predict their values or understand where data has come from in test diagnostics.  Make-it-Easy lets you define a fixed or repeating sequence of values.

A fixed sequence is defined by the `from` function:

```
Maker<Customer> aCustomer = a(Customer, with(name, from("Alice", "Bob", "Carol", "Dave")));

assertThat(make(aCustomer).name(), equalTo("Alice"));
assertThat(make(aCustomer).name(), equalTo("Bob"));
assertThat(make(aCustomer).name(), equalTo("Carol"));
assertThat(make(aCustomer).name(), equalTo("Dave"));
```

A fixed sequence of values will fail if asked to provide more elements than are specified when the sequence is created.  A repeating sequence will start back at the beginning of the sequence when all elements are exhausted:

```
Maker<Customer> aCustomer = a(Customer, with(name, fromRepeating("Alice", "Bob", "Carol", "Dave")));

assertThat(make(aCustomer).name(), equalTo("Alice"));
assertThat(make(aCustomer).name(), equalTo("Bob"));
assertThat(make(aCustomer).name(), equalTo("Carol"));
assertThat(make(aCustomer).name(), equalTo("Dave"));
assertThat(make(aCustomer).name(), equalTo("Alice"));
```

Both fixed and repeating sequences can be created from any Iterable collection of values:

```
SortedSet<String> names = new TreeSet<String>();
names.add("Bob");
names.add("Alice");
names.add("Carol");
names.add("Dave");

Maker<Customer> aCustomer = a(Customer, with(name, from(names)));

assertThat(make(aCustomer).name(), equalTo("Alice"));
assertThat(make(aCustomer).name(), equalTo("Bob"));
assertThat(make(aCustomer).name(), equalTo("Carol"));
assertThat(make(aCustomer).name(), equalTo("Dave"));
```

## Calculated Sequences ##


If we do not want to explicitly specify a sequence of values, we can use some convenient base classes to help us calculate each element of the sequence.

An `IndexedSequence<T>` calculates each element of the sequence from its integer index, starting at zero.

```
Maker<Customer> aCustomer = a(Customer, with(name, new IndexedSequence<String>() {
    protected String valueAt(int index) { return "C"+index); }
}));

assertThat(make(aCustomer).name(), equalTo("C0"));
assertThat(make(aCustomer).name(), equalTo("C1"));
```


A `ChainedSequence<T>` calculates each element of the sequence from the element that preceded it.

```
Maker<Customer> aCustomer = a(Customer, with(name, new ChainedSequence<String>() {
    protected String firstValue() { return "A"; }
    protected String valueAfter(String prevValue) { return prevValue + "'"; }
}));

assertThat(make(aCustomer).name, equalTo("A"));
assertThat(make(aCustomer).name, equalTo("A'"));
```