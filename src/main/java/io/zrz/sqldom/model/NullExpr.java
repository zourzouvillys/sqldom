package io.zrz.sqldom.model;

public class NullExpr implements SqlExpr {

  @Override
  public <R> R apply(Visitor<R> visitor) {
    return visitor.visitNullExpr(this);
  }

  public static NullExpr getInstance() {
    return INSTANCE;
  }

  private static final NullExpr INSTANCE = new NullExpr() {

  };

}
