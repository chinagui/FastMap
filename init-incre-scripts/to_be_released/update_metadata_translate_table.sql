-- Create table
create table SPECIAL
(
  NUM NUMBER(11),
  HZ  VARCHAR2(20),
  PY  VARCHAR2(20)
);

-- Create table
create table WORD_SYMBOL
(
  NUM     NUMBER(11) not null,
  SYMBOL  VARCHAR2(20),
  CORRECT VARCHAR2(50)
);

-- Create table
create table SC_POINT_CHI2ENG_KEYWORD
(
  CHIKEYWORDS NVARCHAR2(255),
  À´Ô´        NVARCHAR2(255),
  ENGKEYWORDS NVARCHAR2(255),
  MEMO        NVARCHAR2(255),
  PRIORITY    NVARCHAR2(255),
  KIND        NVARCHAR2(255),
  ID          NUMBER
);
