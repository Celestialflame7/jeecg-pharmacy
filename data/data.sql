#药品信息
create table drug
(
  id                            varchar(20)          primary key                	                           comment '药品编号',
  name                      varchar(20)          null                                                                 comment '药品名',
  manufacturer        varchar(20)          null       		                           comment '进货单位',
  function                 varchar(40)          null      		                           comment '功能主治',
  dusage                  varchar(20)          null     		                           comment '用法用量',
  price                      double                  null      		                           comment '药品价格',
  produceTime       datetime               null      			         comment '生产日期',
  validityTime         datetime               null                                                                  comment '有效期',
  createDtime         datetime              default CURRENT_TIMESTAMP      null        comment '创建时间',
  updateTime         datetime              default CURRENT_TIMESTAMP      null on update CURRENT_TIMESTAMP
) 
  comment'药品信息';

#员工信息
create table stuff
(
  id                    varchar(20)          primary key comment '员工编号',
  name              varchar(10)          null  comment '员工姓名',
  sex  	        varchar(2)            null comment '性别',
  account          varchar(20)          null comment '账号',
  password       varchar(16)         not null comment '密码',
  position          varchar(10)         null comment '职位',
  location           varchar(20)        null comment '工作地点',
  phone              varchar(16)        null comment '联系方式',
  createDtime    datetime            default CURRENT_TIMESTAMP      null comment '创建时间',
  updateTime    datetime            default CURRENT_TIMESTAMP      null on update CURRENT_TIMESTAMP
) 
  comment'员工信息';

#采购单信息
create table purchase
(
  id                             varchar(20)          primary key comment '采购单编号',
  drugId                    varchar(30)          null  comment '药品编号',
  name                       varchar(30)          null  comment '药品名称',
  manufacturer         varchar(20)            null comment '生产厂商',
  price                        double                null comment '药品价格',
  amount                   varchar(8)          null comment '采购数量',
  total                         varchar(8)         null comment '采购总额',
  time                         varchar(8)        null comment '采购时间',
  number                   varchar(26)        null comment '采购员编号',
  createTime             datetime            default CURRENT_TIMESTAMP      null comment '创建时间',
  updateTime           datetime            default CURRENT_TIMESTAMP      null on update CURRENT_TIMESTAMP,
  foreign key (number) references stuff (id)
) 
  comment '采购单信息';

#退货单信息
create table drugReturn
(
  id                             varchar(20)          primary key comment '退货单编号',
  drugId                    varchar(20)          null  comment '药品编号',
  name                       varchar(30)          null  comment '药品姓名',
  manufacturer         varchar(20)            null comment '生产厂商',
  phone                     varchar(16)            null comment '生产厂商联系方式',
  amount                   int(8)                        null comment '数量',
  total                         float(8)                    null comment '退货总额',
  time                         datetime           null comment '退货时间',
  method                   varchar(20)        null comment '退货方式',
  reason                     text(40)                null comment '退货原因',
  number                   varchar(20)        null comment '操作员编号',
  createTime             datetime            default CURRENT_TIMESTAMP      null comment '创建时间',
  updateTime           datetime            default CURRENT_TIMESTAMP      null on update CURRENT_TIMESTAMP,
  foreign key (number) references stuff (id)
) 
  comment '退货单信息';

#进货单位信息
create table manufacturer
(
  id                             varchar(20)          primary key comment '单位编号',
  name                       varchar(20)          null  comment '单位姓名',
  location                  varchar(20)            null comment '单位地址',
  phone                     varchar(16)            null comment '单位联系方式',
  createTime             datetime            default CURRENT_TIMESTAMP      null comment '创建时间',
  updateTime           datetime            default CURRENT_TIMESTAMP      null on update CURRENT_TIMESTAMP
) 
  comment '进货单位信息';

#仓库信息
create table Warehousing
(
  id                             varchar(20)          primary key comment '仓库编号',
  scale                        varchar(40)          null  comment '仓库规模',
  location                    varchar(20)          null  comment '仓库位置',
  phone                       varchar(20)          null comment '联系方式',
  createTime             datetime              default CURRENT_TIMESTAMP      null comment '创建时间',
  updateTime           datetime              default CURRENT_TIMESTAMP      null on update CURRENT_TIMESTAMP
) 
  comment '仓库信息';

#入库单信息
create table inWarehousing
(
  id                             varchar(20)          primary key comment '入库单编号',
  WarehousingId     varchar(20)          null  comment '进货仓库编号',
  drugId                    varchar(20)          null  comment '药品编号',
  name                       varchar(20)          null comment '药品名称',
  amount                   int(8)                     null comment '药品数量',
  time                         datetime               null comment '入库日期',
  number                   varchar(20)          null comment '操作员编号',
  createTime             datetime              default CURRENT_TIMESTAMP      null comment '创建时间',
  updateTime           datetime              default CURRENT_TIMESTAMP      null on update CURRENT_TIMESTAMP,
  foreign key (WarehousingId) references Warehousing (id),
  foreign key (drugId) references drug (id),
  foreign key (number) references stuff (id)
) 
  comment '入库单信息';

#出库单信息
create table outWarehousing
(
  id                             varchar(20)          primary key comment '出库单编号',
  WarehousingId     varchar(20)          null  comment '出货仓库编号',
  drugId                    varchar(20)          null  comment '药品编号',
  name                       varchar(20)          null comment '药品名称',
  amount                   int(8)                     null comment '药品数量',
  time                         datetime               null comment '出库日期',
  number                   varchar(20)          null comment '操作员编号',
  createTime             datetime              default CURRENT_TIMESTAMP      null comment '创建时间',
  updateTime           datetime              default CURRENT_TIMESTAMP      null on update CURRENT_TIMESTAMP,
  foreign key (WarehousingId) references Warehousing (id),
  foreign key (drugId) references drug (id),
  foreign key (number) references stuff (id)
) 
  comment '出库单信息';

#仓库库存信息
create table stock
(
  id                             varchar(20)          primary key comment '仓库编号',
  drugId                     varchar(20)         null  comment '药品编号',
  name                       varchar(20)          null  comment '药品名称',
  surplus                    int(8)                    null comment '库存余量',
  time                        datetime               null comment '进货日期',
  validity                   datetime               null  comment '有效期',
  createTime             datetime              default CURRENT_TIMESTAMP      null comment '创建时间',
  updateTime           datetime              default CURRENT_TIMESTAMP      null on update CURRENT_TIMESTAMP,
  foreign key (drugId) references drug (id)
) 
  comment '仓库库存信息';

#库存预警
create table warning
(
  id                             varchar(20)          primary key comment '预警单编号',
  drugId                     varchar(20)         null  comment '药品编号',
  name                       varchar(20)          null  comment '药品名称',
  surplus                    int(8)                    null comment '库存余量',
  time                        datetime               null comment '预警时间',
  createTime             datetime              default CURRENT_TIMESTAMP      null comment '创建时间',
  updateTime           datetime              default CURRENT_TIMESTAMP      null on update CURRENT_TIMESTAMP,
  foreign key (drugId) references drug (id)
) 
  comment '库存预警';

#销售单信息
create table sale
(
  id                             varchar(20)          primary key comment '销售单编号',
  drugId                     varchar(20)         null  comment '药品编号',
  name                       varchar(20)          null  comment '药品名称',
  price                         double                null  comment '药品单价',
  amount                    int(8)                    null comment '销售数量',
  total                        double                  null comment '总金额',
  number                   varchar(20)          null comment '操作员编号',
  createTime             datetime              default CURRENT_TIMESTAMP      null comment '创建时间',
  updateTime           datetime              default CURRENT_TIMESTAMP      null on update CURRENT_TIMESTAMP,
  foreign key (drugId) references drug (id),
  foreign key (number) references stuff (id)
) 
  comment '销售单信息';

#药店信息
create table pharmacy
(
  name                       varchar(20)         primary key  comment '药店名称',
  location                   varchar(20)          null  comment '药店地址',
  people                     int(2)                    null  comment '员工数量',
  phone                     varchar(16)                    null comment '联系方式',
  createTime             datetime              default CURRENT_TIMESTAMP      null comment '创建时间',
  updateTime           datetime              default CURRENT_TIMESTAMP      null on update CURRENT_TIMESTAMP
) 
  comment '药店信息';

#药品药店现货信息
create table pharmacyStock
(
  id                             varchar(20)          primary key comment '药品编号',
  drugName              varchar(20)         null  comment '药品名称',
  surplus                    int(8)                    null  comment '药品余量',
  pharmacyName     varchar(20)          null  comment '药店名称',
  createTime             datetime              default CURRENT_TIMESTAMP      null comment '创建时间',
  updateTime           datetime              default CURRENT_TIMESTAMP      null on update CURRENT_TIMESTAMP
) 
  comment '药店现货信息';

#补货单信息
create table replenishment
(
  id                             varchar(20)          primary key comment '补货单编号',
  name                       varchar(20)         null  comment '药店名称',
  drugId                    varchar(20)          null  comment '药品编号',
  amount                    int(8)                    null  comment '药品数量',
  time                         datetime              null comment '发出时间',
  state                         int(1)                    null comment '状态0-未处理，1-已处理',
  createTime             datetime              default CURRENT_TIMESTAMP      null comment '创建时间',
  updateTime           datetime              default CURRENT_TIMESTAMP      null on update CURRENT_TIMESTAMP,
  foreign key (name) references pharmacy (name),
  foreign key (drugId) references drug (id)
) 
  comment '补货单信息';

#调货单信息
create table allocation
(
  id                              varchar(20)          primary key comment '调货单编号',
  sendName              varchar(20)         null  comment '发出方药店名称',
  receiveName          varchar(20)         null  comment '接收方药店名称',
  drugId                     varchar(20)         null  comment '药品编号',
  amount                    int(8)                   null  comment '药品数量',
  time                          datetime              null comment '发出时间',
  state                         int(1)                    null comment '状态0-未处理，1-已处理',
  number                   varchar(20)          null comment '操作员编号',
  createTime             datetime              default CURRENT_TIMESTAMP      null comment '创建时间',
  updateTime           datetime              default CURRENT_TIMESTAMP      null on update CURRENT_TIMESTAMP,
  foreign key (sendName) references pharmacy (name),
  foreign key (receiveName) references pharmacy (name),
  foreign key (drugId) references drug (id),
  foreign key (number) references stuff (id)
) 
  comment '调货单信息';