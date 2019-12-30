public class Son extends Father {
    private String prop ;
    public Son() {
        System.out.println("son cons");
        prop = "son";
    }

    public Human out(){
        System.out.println(prop);
        return this;
    }
    public static void main(String[] args) {
        new Son().out().change();
    }
}
