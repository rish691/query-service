package org.hypertrace.core.query.service.htqueries;

import static org.hypertrace.core.query.service.QueryServiceTestUtils.createFilter;

import java.time.Duration;
import org.hypertrace.core.query.service.api.ColumnIdentifier;
import org.hypertrace.core.query.service.api.Expression;
import org.hypertrace.core.query.service.api.Filter;
import org.hypertrace.core.query.service.api.Function;
import org.hypertrace.core.query.service.api.Operator;
import org.hypertrace.core.query.service.api.OrderByExpression;
import org.hypertrace.core.query.service.api.QueryRequest;
import org.hypertrace.core.query.service.api.QueryRequest.Builder;
import org.hypertrace.core.query.service.api.SortOrder;
import org.hypertrace.core.query.service.api.ValueType;

class ServicesQueries {

  /**
   * [ Select service_id, service_name, COUNT(*) FROM rawServiceView
   * WHERE tenant_id = ? AND ( service_id != ? AND start_time_millis >= ? AND start_time_millis < ?
   * ) GROUP BY service_id, service_name ORDER BY PERCENTILETDIGEST99(duration_millis) desc  limit
   * 10000=Params{integerParams={}, longParams={2=1612271838043, 3=1614691038043},
   * stringParams={0=__default, 1=null}, floatParams={}, doubleParams={}, byteStringParams={}} ]
   */
  static QueryRequest buildQuery1() {
    Builder builder = QueryRequest.newBuilder();
    ColumnIdentifier serviceId = ColumnIdentifier.newBuilder().setColumnName("SERVICE.id").build();
    ColumnIdentifier serviceName = ColumnIdentifier.newBuilder().setColumnName("SERVICE.name").setAlias("SERVICE.name").build();
    Function serviceIdFunction = Function.newBuilder().addArguments(
        Expression.newBuilder().setColumnIdentifier(serviceId).build()).setFunctionName("COUNT")
        .build();

    builder.addSelection(Expression.newBuilder().setColumnIdentifier(serviceId).build());
    builder.addSelection(Expression.newBuilder().setColumnIdentifier(serviceName).build());
    builder.addSelection(Expression.newBuilder().setFunction(serviceIdFunction));

    Filter filter1 =
        createFilter(
            "SERVICE.startTime", Operator.GE,
            ValueType.LONG, System.currentTimeMillis() - Duration.ofHours(1).toMillis());
    Filter filter2 =
        createFilter("SERVICE.startTime", Operator.LT,  ValueType.LONG, System.currentTimeMillis());
    Filter filter3 =
        createFilter("SERVICE.id", Operator.NEQ, ValueType.NULL_STRING, "");

    builder.setFilter(
        Filter.newBuilder()
            .setOperator(Operator.AND)
            .addChildFilter(filter1)
            .addChildFilter(filter2)
            .addChildFilter(filter3)
            .build());


    builder.addGroupBy(
        Expression.newBuilder().setColumnIdentifier(serviceId));
    builder.addGroupBy(
        Expression.newBuilder().setColumnIdentifier(serviceName));
    Function serviceDurationPercentileFunc = Function.newBuilder().addArguments(
        Expression.newBuilder().setColumnIdentifier(ColumnIdentifier.newBuilder().setColumnName("SERVICE.duration").build()).build()).setFunctionName("PERCENTILE99")
        .build();
    OrderByExpression orderByExpression = OrderByExpression.newBuilder().setOrder(SortOrder.DESC).setExpression(
        Expression.newBuilder().setFunction(serviceDurationPercentileFunc).build()).build();
    builder.addOrderBy(orderByExpression);
    builder.setLimit(10000);

    return builder.build();
  }
}
