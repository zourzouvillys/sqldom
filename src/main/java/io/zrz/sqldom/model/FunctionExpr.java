package io.zrz.sqldom.model;

import java.util.List;

import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;

import io.zrz.sqldom.SqlModel;

@Value.Immutable
@Value.Style(visibility = ImplementationVisibility.PUBLIC, depluralize = true)
@Value.Modifiable
public interface FunctionExpr extends SqlExpr {

  String function();

  List<SqlExpr> parameters();

  @Override
  default <R> R apply(Visitor<R> visitor) {
    return visitor.visitFunctionExpr(this);
  }

  default SelectSource toSource() {
    return SqlModel.source(this);
  }

  default SelectSource toSource(String alias) {
    return SqlModel.source(this, alias);
  }

}
