package io.zrz.sqldom.model;

import java.util.List;

import javax.annotation.Nullable;

import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;

@Value.Immutable
@Value.Style(visibility = ImplementationVisibility.PUBLIC)
@Value.Modifiable
public interface FunctionSelectSource extends SelectSource {

  String functionName();

  List<SqlExpr> functionParameters();

  @Nullable
  String alias();

  @Override
  default String exportedName() {
    return alias();
  }

  @Override
  default <R> R apply(Visitor<R> context) {
    return context.visitFunctionSelectSource(this);
  }

}
