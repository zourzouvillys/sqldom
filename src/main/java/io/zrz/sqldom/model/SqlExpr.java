package io.zrz.sqldom.model;

import io.zrz.sqldom.SqlModel;

public interface SqlExpr {

  interface Visitor<R> {

    R visitNullExpr(NullExpr expr);

    R visitStringExpr(StringExpr expr);

    R visitIdentExpr(IdentExpr expr);

    R visitFunctionExpr(FunctionExpr expr);

    R visitSelectExpr(SelectExpr expr);

    R visitAggrFuncExpr(AggrFuncExpr expr);

    R visitWindowFuncExpr(WindowFuncExpr expr);

    R visitCastExpr(CastExpr expr);

    R visitBinaryExpr(BinaryExpr expr);

    R visitIntExpr(IntExpr expr);

    R visitBoolExpr(BoolExpr expr);

    R visitParamExpr(ParamExpr expr);

    R visitTableRefExpr(TableRefExpr expr);

    R visitRawExpr(RawExpr rawExpr);

    R visitArrayFuncExpr(ArrayExpr arrayExpr);

    R visitSelectSourceExpr(SelectSource arrayExpr);

  }

  <R> R apply(Visitor<R> visitor);

  default BinaryExpr and(SqlExpr rhs) {
    return SqlModel.binop("AND", this, rhs);
  }

  default SqlExpr wrap(String op, SqlExpr right) {

    return SqlModel.binop(op, this, right);

  }

  default SqlExpr coalesce(SqlExpr val) {

    return SqlModel.function("COALESCE", this, val);

  }

}
