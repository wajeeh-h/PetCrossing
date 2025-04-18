import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit test class for Tuple
 */
public class TupleTest {
    
    @Test
    @DisplayName("Test constructor initializes fields correctly")
    void testConstructor() {
        // Create a simple Integer, String tuple
        Integer x = 42;
        String y = "test";
        Tuple<Integer, String> tuple = new Tuple<>(x, y);
        
        // Verify fields match constructor arguments
        assertEquals(x, tuple.x);
        assertEquals(y, tuple.y);
    }
    
    @Test
    @DisplayName("Test with different types")
    void testDifferentTypes() {
        // Test with various type combinations
        
        // String, Integer
        Tuple<String, Integer> t1 = new Tuple<>("hello", 123);
        assertEquals("hello", t1.x);
        assertEquals(123, t1.y);
        
        // Boolean, Double
        Tuple<Boolean, Double> t2 = new Tuple<>(true, 3.14);
        assertEquals(true, t2.x);
        assertEquals(3.14, t2.y);
        
        // Custom object types
        Person person = new Person("John", 30);
        Address address = new Address("123 Main St");
        Tuple<Person, Address> t3 = new Tuple<>(person, address);
        assertEquals(person, t3.x);
        assertEquals(address, t3.y);
        assertEquals("John", t3.x.name);
        assertEquals("123 Main St", t3.y.street);
    }
    
    @Test
    @DisplayName("Test with same types")
    void testSameTypes() {
        // Both elements same type (String)
        Tuple<String, String> t1 = new Tuple<>("first", "second");
        assertEquals("first", t1.x);
        assertEquals("second", t1.y);
        
        // Both elements same type (Integer)
        Tuple<Integer, Integer> t2 = new Tuple<>(1, 2);
        assertEquals(1, t2.x);
        assertEquals(2, t2.y);
    }
    
    @Test
    @DisplayName("Test with null values")
    void testWithNullValues() {
        // Test with null for first value
        Tuple<String, Integer> t1 = new Tuple<>(null, 123);
        assertNull(t1.x);
        assertEquals(123, t1.y);
        
        // Test with null for second value
        Tuple<String, Integer> t2 = new Tuple<>("hello", null);
        assertEquals("hello", t2.x);
        assertNull(t2.y);
        
        // Test with both values null
        Tuple<String, Integer> t3 = new Tuple<>(null, null);
        assertNull(t3.x);
        assertNull(t3.y);
    }
    
    @Test
    @DisplayName("Test with nested tuples")
    void testNestedTuples() {
        // Create nested tuples
        Tuple<Integer, String> inner = new Tuple<>(42, "inner");
        Tuple<String, Tuple<Integer, String>> outer = new Tuple<>("outer", inner);
        
        // Verify values
        assertEquals("outer", outer.x);
        assertSame(inner, outer.y);
        assertEquals(42, outer.y.x);
        assertEquals("inner", outer.y.y);
    }
    
    @Test
    @DisplayName("Test immutability")
    void testImmutability() {
        // Verify that fields are final and can't be changed
        Tuple<StringBuilder, StringBuilder> tuple = new Tuple<>(
            new StringBuilder("first"), 
            new StringBuilder("second")
        );
        
        // While we can't modify the references, we can modify the objects they point to
        tuple.x.append(" modified");
        tuple.y.append(" modified");
        
        assertEquals("first modified", tuple.x.toString());
        assertEquals("second modified", tuple.y.toString());
        
        // We confirm we can't reassign the fields (this would cause a compilation error)
        // tuple.x = new StringBuilder("new");  // Won't compile - final field
        // tuple.y = new StringBuilder("new");  // Won't compile - final field
    }
    
    // Helper classes for testing custom object types
    
    private static class Person {
        public String name;
        public int age;
        
        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }
    
    private static class Address {
        public String street;
        
        public Address(String street) {
            this.street = street;
        }
    }
}