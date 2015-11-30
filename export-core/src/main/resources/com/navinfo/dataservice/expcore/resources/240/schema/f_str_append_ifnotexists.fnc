create or replace function f_str_append_ifnotexists(p_target varchar2,
                                                    p_seg    varchar2)
  return varchar2 deterministic is
  v_target      varchar2(32767);
  l_comma_index pls_integer;
  l_index       pls_integer := 1;
  v_seg         varchar2(32767);
  v_seg2        varchar2(32767);
begin
  v_seg2 := p_seg;
  if p_seg is null then
    return p_target;
  end if;
  if p_target is null then
    v_target := p_seg;
  else
    if substr(p_target, -1, 1) <> '|' then
      v_target := p_target || '|';
    else
      v_target := p_target;
    end if;
    if substr(p_target, 1, 1) <> '|' then
      v_target := '|' || v_target;
    end if;
    if instr(p_seg, '|') = 0 then
      if instr(v_target, '|' || p_seg || '|') = 0 then
        v_target := v_target || p_seg;
      end if;
    else
      --如果拼接字符串结果不是"|"结尾则改为“|”结尾，再进行合并
      if substr(p_seg, -1, 1) <> '|' then
        v_seg2 := p_seg || '|';
      end if;
      loop
        l_comma_index := instr(v_seg2, '|', l_index);
        exit when l_comma_index = 0;
        v_seg := substr(v_seg2, l_index, l_comma_index - l_index);
        if trim(v_seg) is not null and
           instr(v_target, '|' || v_seg || '|') = 0 then
          v_target := v_target || v_seg || '|';
        end if;
        l_index := l_comma_index + 1;
      end loop;
    end if;
  end if;
  --如果最后的结果结尾不是"|"，结尾添加“|”
  if substr(v_target, -1, 1) <> '|' then
    v_target := v_target || '|';
  end if;
  --如果最后开头是"|"，则删除开头的“|”
  if v_target is not null and instr(v_target, '|') = 1 then
    v_target := substr(v_target, 2, length(v_target));
  end if;
  return v_target;
end;
/
