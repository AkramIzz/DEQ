# QED
A Java interpreter for QED programming language

## QED?
QED is an abbreviation of the Latin words "Quod Erat Demonstrandum" which loosely translated means "that which was to be demonstrated".
It is usually placed at the end of a mathematical proof to indicate that the proof is complete.

# Features
## Closures
```js
fun make_counter(n) {
  var count = n - 1;
  fun counter() {
    counter = counter + 1;
    return counter;
  }
  return counter;
}

var counter = make_counter(0);
print counter(); // 0
print counter(); // 1
```

## Arrays
```js
var arr = [1, 2, nil, "Hello"];
print arr[2]; // nil
print arr[4]; // index out of range error
arr[0] = 0;
print arr; // [0, 2, nil, "Hello"];
```

## Classes and Inheritance
```js
class Vehicle {
  fun init(wheels) {
    this.wheels = wheels;
  }
  
  fun start() {
    ...
  }
}

class Car > Vehicle {
  fun init() {
    super.init(4);
  }
  
  fun start() {
    super.start();
    ...
  }
}

var car = Car();
print car.wheels; // 4
```
