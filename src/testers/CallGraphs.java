package testers;

class CallGraphs {
    public static void main(String[] args) {
        doStuff();
        A a = new A();
        a.baz();
    }

    public static void doStuff() {
        new A().foo();
        B b = new B();
        b.method1();
        b.method2();
        C c = new C();
        c.methodA();
        D d = new D();
        d.methodB();
    }
}

class A {
    public void foo() {
        bar();
        C c = new C();
        c.methodA();
    }

    public void bar() {
        System.out.println("A.bar() called.");
        D d = new D();
        d.methodB();
    }

    public void baz() {
        System.out.println("A.baz() called.");
    }
}

class B {
    public void method1() {
        System.out.println("B.method1() called.");
    }

    public void method2() {
        System.out.println("B.method2() called.");
    }
}

class C {
    public void methodA() {
        System.out.println("C.methodA() called.");
        B b = new B();
        b.method1();
    }
}

class D {
    public void methodB() {
        System.out.println("D.methodB() called.");
    }
}
