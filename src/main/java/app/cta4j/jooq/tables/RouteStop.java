/*
 * This file is generated by jOOQ.
 */
package app.cta4j.jooq.tables;


import app.cta4j.jooq.Keys;
import app.cta4j.jooq.Public;
import app.cta4j.jooq.tables.Direction.DirectionPath;
import app.cta4j.jooq.tables.Route.RoutePath;
import app.cta4j.jooq.tables.Stop.StopPath;
import app.cta4j.jooq.tables.records.RouteStopRecord;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.InverseForeignKey;
import org.jooq.Name;
import org.jooq.Path;
import org.jooq.PlainSQL;
import org.jooq.QueryPart;
import org.jooq.Record;
import org.jooq.SQL;
import org.jooq.Schema;
import org.jooq.Select;
import org.jooq.Stringly;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class RouteStop extends TableImpl<RouteStopRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.route_stop</code>
     */
    public static final RouteStop ROUTE_STOP = new RouteStop();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<RouteStopRecord> getRecordType() {
        return RouteStopRecord.class;
    }

    /**
     * The column <code>public.route_stop.route_id</code>.
     */
    public final TableField<RouteStopRecord, String> ROUTE_ID = createField(DSL.name("route_id"), SQLDataType.VARCHAR(4).nullable(false), this, "");

    /**
     * The column <code>public.route_stop.direction_id</code>.
     */
    public final TableField<RouteStopRecord, Integer> DIRECTION_ID = createField(DSL.name("direction_id"), SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>public.route_stop.stop_id</code>.
     */
    public final TableField<RouteStopRecord, Integer> STOP_ID = createField(DSL.name("stop_id"), SQLDataType.INTEGER.nullable(false), this, "");

    private RouteStop(Name alias, Table<RouteStopRecord> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private RouteStop(Name alias, Table<RouteStopRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>public.route_stop</code> table reference
     */
    public RouteStop(String alias) {
        this(DSL.name(alias), ROUTE_STOP);
    }

    /**
     * Create an aliased <code>public.route_stop</code> table reference
     */
    public RouteStop(Name alias) {
        this(alias, ROUTE_STOP);
    }

    /**
     * Create a <code>public.route_stop</code> table reference
     */
    public RouteStop() {
        this(DSL.name("route_stop"), null);
    }

    public <O extends Record> RouteStop(Table<O> path, ForeignKey<O, RouteStopRecord> childPath, InverseForeignKey<O, RouteStopRecord> parentPath) {
        super(path, childPath, parentPath, ROUTE_STOP);
    }

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    public static class RouteStopPath extends RouteStop implements Path<RouteStopRecord> {

        private static final long serialVersionUID = 1L;
        public <O extends Record> RouteStopPath(Table<O> path, ForeignKey<O, RouteStopRecord> childPath, InverseForeignKey<O, RouteStopRecord> parentPath) {
            super(path, childPath, parentPath);
        }
        private RouteStopPath(Name alias, Table<RouteStopRecord> aliased) {
            super(alias, aliased);
        }

        @Override
        public RouteStopPath as(String alias) {
            return new RouteStopPath(DSL.name(alias), this);
        }

        @Override
        public RouteStopPath as(Name alias) {
            return new RouteStopPath(alias, this);
        }

        @Override
        public RouteStopPath as(Table<?> alias) {
            return new RouteStopPath(alias.getQualifiedName(), this);
        }
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public UniqueKey<RouteStopRecord> getPrimaryKey() {
        return Keys.ROUTE_STOP_PKEY;
    }

    @Override
    public List<ForeignKey<RouteStopRecord, ?>> getReferences() {
        return Arrays.asList(Keys.ROUTE_STOP__ROUTE_STOP_DIRECTION_ID_FKEY, Keys.ROUTE_STOP__ROUTE_STOP_ROUTE_ID_FKEY, Keys.ROUTE_STOP__ROUTE_STOP_STOP_ID_FKEY);
    }

    private transient DirectionPath _direction;

    /**
     * Get the implicit join path to the <code>public.direction</code> table.
     */
    public DirectionPath direction() {
        if (_direction == null)
            _direction = new DirectionPath(this, Keys.ROUTE_STOP__ROUTE_STOP_DIRECTION_ID_FKEY, null);

        return _direction;
    }

    private transient RoutePath _route;

    /**
     * Get the implicit join path to the <code>public.route</code> table.
     */
    public RoutePath route() {
        if (_route == null)
            _route = new RoutePath(this, Keys.ROUTE_STOP__ROUTE_STOP_ROUTE_ID_FKEY, null);

        return _route;
    }

    private transient StopPath _stop;

    /**
     * Get the implicit join path to the <code>public.stop</code> table.
     */
    public StopPath stop() {
        if (_stop == null)
            _stop = new StopPath(this, Keys.ROUTE_STOP__ROUTE_STOP_STOP_ID_FKEY, null);

        return _stop;
    }

    @Override
    public RouteStop as(String alias) {
        return new RouteStop(DSL.name(alias), this);
    }

    @Override
    public RouteStop as(Name alias) {
        return new RouteStop(alias, this);
    }

    @Override
    public RouteStop as(Table<?> alias) {
        return new RouteStop(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public RouteStop rename(String name) {
        return new RouteStop(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public RouteStop rename(Name name) {
        return new RouteStop(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public RouteStop rename(Table<?> name) {
        return new RouteStop(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public RouteStop where(Condition condition) {
        return new RouteStop(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public RouteStop where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public RouteStop where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public RouteStop where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public RouteStop where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public RouteStop where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public RouteStop where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public RouteStop where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public RouteStop whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public RouteStop whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}
