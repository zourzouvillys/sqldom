package io.zrz.sqldom.model;

import java.util.List;

import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;

@Value.Immutable
@Value.Style(visibility = ImplementationVisibility.PUBLIC, depluralize = true)
@Value.Modifiable
public interface ArrayExpr extends SqlExpr {

  List<SqlExpr> values();

  @Override
  default <R> R apply(Visitor<R> visitor) {
    return visitor.visitArrayFuncExpr(this);
  }

}
