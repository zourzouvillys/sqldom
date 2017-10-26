package io.zrz.sqldom.model;

import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;

@Value.Immutable
@Value.Style(visibility = ImplementationVisibility.PUBLIC, depluralize = true)
@Value.Modifiable
public interface BinaryExpr extends SqlExpr {

  SqlExpr left();

  SqlExpr right();

  String operator();

  @Override
  default <R> R apply(Visitor<R> visitor) {
    return visitor.visitBinaryExpr(this);
  }

}
