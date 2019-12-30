public class Father implements Human{
    private String prop ;
    public Father() {
        System.out.println("father cons");
        prop = "father";
    }

    @Override
    public Human out(){
        System.out.println(prop);
        return this;
    }

    public Human change(){
        System.out.println(prop + "change");
        return this;
    }
}
