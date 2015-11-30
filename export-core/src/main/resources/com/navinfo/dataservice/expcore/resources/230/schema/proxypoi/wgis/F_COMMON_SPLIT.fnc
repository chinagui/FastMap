create or replace function F_COMMON_SPLIT
(
 v_str            in       varchar2,
 v_spliter        in       varchar2
)
return t_varchar_array is
  p_array                t_varchar_array;
  p_pk                   varchar2(32);
  p_substr               varchar2(4000);
  p_j                    integer;
begin
      p_array := t_varchar_array();
      p_substr := v_str;
      p_j := 1;
      if instr(v_str, v_spliter, 1, 1) = 0
      then
        p_array.extend;
        p_array(p_j) := v_str;
        p_j := p_j + 1;
      else
        while instr(p_substr, v_spliter, 1, 1) > 0
        loop
          P_pk := substr(p_substr, 1, instr(p_substr, v_spliter, 1, 1) - 1);
          p_array.extend;
          p_array(p_j) := P_pk;
          p_substr := substr(p_substr, instr(p_substr, v_spliter, 1, 1) + 1, length(p_substr));
          p_j := p_j + 1;
        end loop;
        if p_substr is not null
        then
          p_array.extend;
          p_array(p_j) := p_substr;
          p_j := p_j + 1;
        end if;
      end if;
  return(p_array);
end F_COMMON_SPLIT;
/
