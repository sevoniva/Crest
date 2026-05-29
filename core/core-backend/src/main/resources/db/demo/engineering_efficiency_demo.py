#!/usr/bin/env python3
"""Generate a repeatable Crest engineering efficiency demo dataset.

The script prints SQL used by V1.3__demo_engineering_efficiency.sql.
Run it into the local MySQL container while iterating:

  python3 .crest-local/engineering_efficiency_demo.py > .crest-local/engineering_efficiency_demo.sql
  docker exec -i mysql-crest-local mysql -uroot -p'Password123@mysql' < .crest-local/engineering_efficiency_demo.sql
"""

from __future__ import annotations

import copy
import json
import random
from collections import defaultdict
from datetime import date, timedelta


RNG = random.Random(20260528)
TODAY = date(2026, 5, 28)
START = date(2026, 1, 1)
NOW_MS = 1779928800000
DATASOURCE_ID = 910001
SCREEN_ID = 980001


LIGHT_THEME = {
    "primary": "#3B82F6",
    "secondary": "#6E62E8",
    "accent": "#F5A623",
    "teal": "#1FB6A6",
    "rose": "#EF5A7A",
    "green": "#45B36B",
    "sky": "#38BDF8",
    "orange": "#F97316",
    "bg": "#F5F8FF",
    "surface": "#FFFFFF",
    "surface_alt": "#F8FBFF",
    "border": "rgba(59,130,246,.16)",
    "grid": "rgba(30,64,175,.10)",
    "text": "#172033",
    "muted": "#64748B",
    "soft_blue": "#EAF2FF",
    "soft_teal": "#EAFBF8",
    "area_border": "#BFD7FF",
    "surface_alpha": "rgba(255,255,255,.92)",
    "tooltip_bg": "rgba(255,255,255,.96)",
    "scrollbar": "rgba(59,130,246,.35)",
    "axis_line": "rgba(59,130,246,.22)",
    "title_shadow": "0 8px 26px rgba(59,130,246,.18)",
}
LIGHT_THEME["palette"] = [
    LIGHT_THEME["primary"],
    LIGHT_THEME["secondary"],
    LIGHT_THEME["accent"],
    LIGHT_THEME["teal"],
]
LIGHT_THEME["chart_palette"] = LIGHT_THEME["palette"] + [LIGHT_THEME["rose"], LIGHT_THEME["green"]]
LIGHT_THEME["flow_palette"] = LIGHT_THEME["palette"] + [
    LIGHT_THEME["rose"],
    LIGHT_THEME["green"],
    LIGHT_THEME["sky"],
    LIGHT_THEME["orange"],
]


def sql_literal(value):
    if value is None:
        return "NULL"
    if isinstance(value, bool):
        return "1" if value else "0"
    if isinstance(value, (int, float)):
        return str(value)
    if isinstance(value, date):
        return "'" + value.isoformat() + "'"
    return "'" + str(value).replace("\\", "\\\\").replace("'", "''") + "'"


def json_text(value):
    return json.dumps(value, ensure_ascii=False, separators=(",", ":"))


def json_sql(value):
    return sql_literal(json_text(value))


def insert_many(table, columns, rows, chunk_size=400):
    if not rows:
        return []
    chunks = []
    for i in range(0, len(rows), chunk_size):
        chunk = rows[i : i + chunk_size]
        values = []
        for row in chunk:
            values.append("(" + ",".join(sql_literal(v) for v in row) + ")")
        chunks.append(
            f"INSERT INTO {table} ({','.join(columns)}) VALUES\n"
            + ",\n".join(values)
            + ";"
        )
    return chunks


def weekday_range(start, end):
    day = start
    while day <= end:
        if day.weekday() < 5:
            yield day
        day += timedelta(days=1)


def month_starts():
    return [date(2026, m, 1) for m in range(1, 6)]


TEAMS = [
    (1, "基础平台部", "开发者平台", "平台技术群", "林知远", "RD-PLATFORM"),
    (2, "交易履约部", "订单履约", "交易技术群", "周行舟", "RD-COMMERCE"),
    (3, "数据智能部", "数据平台", "数据智能群", "沈明澈", "RD-DATA"),
    (4, "客户增长部", "增长中台", "增长业务群", "许念安", "RD-GROWTH"),
    (5, "移动体验部", "App体验", "终端体验群", "陈景然", "RD-MOBILE"),
    (6, "质量工程部", "质量效能", "工程效能群", "顾清和", "RD-QA"),
]

VENDORS = [
    (101, "云栖软件", "驻场开发", "华东", "人月外包", "S1", 780, "合作中"),
    (102, "北辰科技", "交付项目", "华北", "项目制", "S2", 720, "合作中"),
    (103, "海岚咨询", "测试服务", "华南", "混合计费", "S1", 560, "合作中"),
    (104, "星河研发", "算法数据", "华东", "人月外包", "S2", 860, "合作中"),
    (105, "青藤工程", "运维支持", "西南", "驻场支持", "S3", 520, "观察中"),
]

ROLE_PLAN = {
    1: {"架构师": 2, "后端开发": 6, "前端开发": 2, "测试工程师": 2, "DevOps": 4, "产品经理": 1, "项目经理": 1, "数据工程师": 1},
    2: {"架构师": 1, "后端开发": 7, "前端开发": 3, "测试工程师": 4, "DevOps": 2, "产品经理": 2, "项目经理": 2, "数据工程师": 1},
    3: {"架构师": 1, "后端开发": 3, "前端开发": 1, "测试工程师": 2, "DevOps": 2, "产品经理": 1, "项目经理": 1, "数据工程师": 7},
    4: {"架构师": 1, "后端开发": 4, "前端开发": 5, "测试工程师": 3, "DevOps": 1, "产品经理": 3, "项目经理": 1, "数据工程师": 2},
    5: {"架构师": 1, "后端开发": 3, "前端开发": 6, "测试工程师": 4, "DevOps": 1, "产品经理": 2, "项目经理": 1, "数据工程师": 1},
    6: {"架构师": 1, "后端开发": 2, "前端开发": 1, "测试工程师": 8, "DevOps": 3, "产品经理": 1, "项目经理": 2, "数据工程师": 2},
}

ROLE_CAPACITY = {
    "架构师": 150,
    "后端开发": 162,
    "前端开发": 160,
    "测试工程师": 158,
    "DevOps": 164,
    "产品经理": 150,
    "项目经理": 146,
    "数据工程师": 160,
}

ROLE_COST = {
    "架构师": 980,
    "后端开发": 680,
    "前端开发": 650,
    "测试工程师": 520,
    "DevOps": 700,
    "产品经理": 620,
    "项目经理": 650,
    "数据工程师": 740,
}

ROLE_EXTERNAL_RATE = {
    "架构师": 0.12,
    "后端开发": 0.38,
    "前端开发": 0.32,
    "测试工程师": 0.55,
    "DevOps": 0.26,
    "产品经理": 0.10,
    "项目经理": 0.18,
    "数据工程师": 0.34,
}

PROJECTS = [
    (301, "RDE-001", "统一权限中心", 1, "研发平台", "P1", "刘一鸣"),
    (302, "RDE-002", "灰度发布平台", 1, "发布体系", "P0", "唐云舒"),
    (303, "RDE-003", "开发者门户二期", 1, "开发体验", "P2", "宋清予"),
    (304, "ORD-001", "订单风控引擎", 2, "交易安全", "P0", "谢知秋"),
    (305, "ORD-002", "履约时效提升", 2, "履约体验", "P1", "江南川"),
    (306, "ORD-003", "海外结算一期", 2, "跨境结算", "P1", "郑微澜"),
    (307, "DAT-001", "数据资产目录", 3, "数据治理", "P1", "苏见月"),
    (308, "DAT-002", "推荐策略平台", 3, "智能推荐", "P0", "马行简"),
    (309, "DAT-003", "AIGC知识库", 3, "智能助手", "P2", "叶青岚"),
    (310, "GRO-001", "会员增长实验", 4, "增长实验", "P1", "秦以安"),
    (311, "GRO-002", "智能客服接入", 4, "客户运营", "P2", "何星河"),
    (312, "GRO-003", "低代码活动页", 4, "营销效率", "P2", "夏知夏"),
    (313, "APP-001", "移动端性能治理", 5, "App体验", "P0", "程砚秋"),
    (314, "APP-002", "搜索体验优化", 5, "App转化", "P1", "罗明远"),
    (315, "APP-003", "离线包治理", 5, "App稳定性", "P2", "姚清宁"),
    (316, "QA-001", "质量门禁升级", 6, "测试平台", "P0", "陆景行"),
    (317, "QA-002", "测试数据工厂", 6, "测试效率", "P1", "温若水"),
    (318, "QA-003", "研发效能看板", 6, "效能度量", "P1", "韩知微"),
]


def build_employees():
    surnames = "赵钱孙李周吴郑王冯陈褚卫蒋沈韩杨朱秦尤许何吕施张孔曹严华金魏陶姜谢邹喻柏水窦章云苏潘葛奚范彭郎鲁韦昌马苗凤花方俞任袁柳鲍史唐费廉岑薛雷贺倪汤"
    given = [
        "子昂",
        "景行",
        "知夏",
        "清远",
        "一诺",
        "云舟",
        "明澈",
        "星河",
        "若水",
        "以安",
        "思远",
        "南川",
        "念安",
        "青岚",
        "行简",
        "微澜",
        "清予",
        "知秋",
        "嘉言",
        "砚秋",
    ]
    rows = []
    employee_id = 2001
    name_index = 0
    vendor_ids = [v[0] for v in VENDORS]
    for team_id, role_counts in ROLE_PLAN.items():
        for role_name, count in role_counts.items():
            for _ in range(count):
                external = RNG.random() < ROLE_EXTERNAL_RATE[role_name]
                vendor_id = RNG.choice(vendor_ids) if external else None
                employment_type = "供应商" if external else "内部"
                seniority = RNG.choices(
                    ["初级", "中级", "高级", "专家"],
                    weights=[12, 42, 36, 10],
                )[0]
                start_date = START - timedelta(days=RNG.randint(20, 1000))
                name = surnames[name_index % len(surnames)] + given[name_index % len(given)]
                name_index += 1
                rows.append(
                    (
                        employee_id,
                        name,
                        team_id,
                        vendor_id,
                        employment_type,
                        role_name,
                        seniority,
                        ROLE_COST[role_name] + RNG.randint(-60, 80),
                        start_date,
                        ROLE_CAPACITY[role_name] + RNG.randint(-8, 8),
                        "在岗",
                    )
                )
                employee_id += 1
    return rows


EMPLOYEES = build_employees()
EMPLOYEE_BY_ID = {row[0]: row for row in EMPLOYEES}
PROJECT_BY_ID = {row[0]: row for row in PROJECTS}
PROJECTS_BY_TEAM = defaultdict(list)
for project in PROJECTS:
    PROJECTS_BY_TEAM[project[3]].append(project)


def build_requirements():
    rows = []
    req_id = 400001
    req_types = ["业务需求", "技术优化", "缺陷修复", "合规安全", "数据需求"]
    for project in PROJECTS:
        project_id, project_code, _project_name, team_id, _domain, default_priority, _owner = project
        for index in range(RNG.randint(20, 32)):
            created = START + timedelta(days=RNG.randint(0, (TODAY - START).days - 2))
            req_type = RNG.choices(req_types, weights=[42, 22, 18, 8, 10])[0]
            priority = RNG.choices(["P0", "P1", "P2", "P3"], weights=[12, 42, 36, 10])[0]
            if index % 11 == 0:
                priority = default_priority
            story_points = RNG.choices([1, 2, 3, 5, 8, 13, 21], weights=[8, 12, 20, 28, 20, 9, 3])[0]
            rework_count = RNG.choices([0, 1, 2, 3], weights=[58, 28, 11, 3])[0]
            discovery_days = RNG.randint(1, 5) + (1 if priority == "P0" else 0)
            design_days = RNG.randint(1, 7) + (1 if story_points >= 13 else 0)
            dev_days = RNG.randint(3, 9) + max(0, story_points // 3) + rework_count * 2
            test_days = RNG.randint(2, 7) + rework_count
            waiting_days = RNG.randint(0, 9)
            lead_time_days = discovery_days + design_days + dev_days + test_days + waiting_days
            planned_online = created + timedelta(days=lead_time_days + RNG.randint(-2, 5))
            actual_online = planned_online + timedelta(days=RNG.randint(-3, 8))
            elapsed = (TODAY - created).days
            delivery_slip_rate = (
                0.14
                + rework_count * 0.04
                + (0.08 if story_points >= 13 else 0)
                + (0.05 if req_type in {"合规安全", "数据需求"} else 0)
            )
            delivered = actual_online <= TODAY and RNG.random() > min(delivery_slip_rate, 0.36)
            paused = not delivered and elapsed > 55 and RNG.random() < 0.11
            if delivered:
                status = "已上线"
                online_date = actual_online
            elif paused:
                status = "暂缓"
                online_date = None
            else:
                progress = elapsed / max(lead_time_days, 1)
                if progress < 0.12:
                    status = "需求池"
                elif progress < 0.28:
                    status = "分析澄清"
                elif progress < 0.40:
                    status = "就绪待开发"
                elif progress < 0.68:
                    status = "开发中"
                elif progress < 0.78:
                    status = "代码评审"
                elif progress < 0.90:
                    status = "测试验证"
                else:
                    status = "待发布"
                online_date = None
            vendor_id = RNG.choice([None, None, 101, 102, 103, 104, 105])
            if req_type == "缺陷修复":
                vendor_id = RNG.choice([None, 103, 101])
            demand_hours = round(story_points * RNG.uniform(8.5, 13.5) + rework_count * 7, 1)
            rows.append(
                (
                    req_id,
                    f"{project_code}-REQ-{index + 1:03d}",
                    project_id,
                    team_id,
                    vendor_id,
                    req_type,
                    priority,
                    status,
                    created,
                    planned_online,
                    online_date,
                    lead_time_days if delivered else (TODAY - created).days,
                    discovery_days,
                    design_days,
                    dev_days,
                    test_days,
                    waiting_days,
                    rework_count,
                    story_points,
                    demand_hours,
                    1 if delivered else 0,
                )
            )
            req_id += 1
    return rows


REQUIREMENTS = build_requirements()
REQ_BY_PROJECT = defaultdict(list)
for req in REQUIREMENTS:
    REQ_BY_PROJECT[req[2]].append(req)


PROJECT_LOAD_SCORE = {}
for project in PROJECTS:
    reqs = REQ_BY_PROJECT[project[0]]
    score = 1.0
    for req in reqs:
        priority = req[6]
        delivered = bool(req[20])
        recency = 1.5 if req[8] >= date(2026, 4, 1) else 0.8
        urgency = {"P0": 1.8, "P1": 1.35, "P2": 1.0, "P3": 0.75}.get(priority, 1.0)
        open_boost = 1.4 if not delivered else 0.85
        score += req[18] * recency * urgency * open_boost
    PROJECT_LOAD_SCORE[project[0]] = score


def pick_project_for_employee(employee):
    team_id = employee[2]
    projects = PROJECTS_BY_TEAM[team_id]
    weights = [PROJECT_LOAD_SCORE[project[0]] for project in projects]
    return RNG.choices(projects, weights=weights)[0]


def pick_requirement(project_id, current_day):
    candidates = [req for req in REQ_BY_PROJECT[project_id] if req[8] <= current_day]
    if not candidates:
        return None
    return RNG.choice(candidates)


def build_worklogs():
    work_types = {
        "架构师": ["技术设计", "代码评审", "方案评审", "风险治理"],
        "后端开发": ["开发", "代码评审", "缺陷修复", "联调"],
        "前端开发": ["开发", "联调", "缺陷修复", "体验优化"],
        "测试工程师": ["测试执行", "缺陷验证", "自动化测试", "质量分析"],
        "DevOps": ["发布运维", "自动化建设", "环境治理", "应急支持"],
        "产品经理": ["需求分析", "验收", "用户研究", "会议评审"],
        "项目经理": ["项目协调", "供应商管理", "风险同步", "排期跟踪"],
        "数据工程师": ["开发", "数据建模", "数据治理", "联调"],
    }
    rows = []
    worklog_id = 500001
    for day in weekday_range(START, TODAY):
        for employee in EMPLOYEES:
            role = employee[5]
            attendance_rate = 0.78 if employee[4] == "内部" else 0.84
            if day.weekday() == 4:
                attendance_rate -= 0.06
            if RNG.random() > attendance_rate:
                continue
            project = pick_project_for_employee(employee)
            req = pick_requirement(project[0], day)
            work_type = RNG.choice(work_types[role])
            base_hours = RNG.uniform(5.2, 8.4)
            if work_type in {"会议评审", "风险同步", "排期跟踪"}:
                base_hours = RNG.uniform(2.0, 5.0)
            hours = round(base_hours, 1)
            billable = hours if employee[4] == "供应商" else 0
            rows.append(
                (
                    worklog_id,
                    day,
                    employee[0],
                    project[0],
                    req[0] if req else None,
                    work_type,
                    hours,
                    round(billable, 1),
                )
            )
            worklog_id += 1
    return rows


WORKLOGS = build_worklogs()


def build_code_daily():
    rows = []
    code_id = 600001
    code_roles = {"架构师", "后端开发", "前端开发", "DevOps", "数据工程师", "测试工程师"}
    categories = ["feature", "bugfix", "refactor", "test", "ops"]
    languages = {
        "后端开发": ["Java", "Kotlin", "SQL"],
        "前端开发": ["TypeScript", "Vue", "CSS"],
        "测试工程师": ["Python", "Java", "SQL"],
        "DevOps": ["Shell", "Go", "YAML"],
        "数据工程师": ["SQL", "Python", "Scala"],
        "架构师": ["Java", "TypeScript", "SQL"],
    }
    for day in weekday_range(START, TODAY):
        for employee in EMPLOYEES:
            role = employee[5]
            if role not in code_roles:
                continue
            commit_rate = 0.48
            if role in {"后端开发", "前端开发", "数据工程师"}:
                commit_rate = 0.62
            elif role == "测试工程师":
                commit_rate = 0.28
            if RNG.random() > commit_rate:
                continue
            project = pick_project_for_employee(employee)
            req = pick_requirement(project[0], day)
            category = RNG.choices(categories, weights=[42, 22, 16, 13, 7])[0]
            commit_count = RNG.randint(1, 5)
            added = int(RNG.triangular(20, 520, 120) * commit_count)
            deleted = int(RNG.triangular(5, 260, 60) * commit_count)
            if category == "refactor":
                deleted = int(deleted * 1.45)
            rows.append(
                (
                    code_id,
                    day,
                    employee[0],
                    project[0],
                    req[0] if req else None,
                    f"{project[1].lower()}-service",
                    RNG.choice(languages[role]),
                    category,
                    commit_count,
                    added,
                    deleted,
                    RNG.randint(2, 28),
                )
            )
            code_id += 1
    return rows


CODE_DAILY = build_code_daily()


def build_capacity():
    req_by_month_team = defaultdict(float)
    role_coeff = {
        "架构师": 1.1,
        "后端开发": 4.0,
        "前端开发": 2.6,
        "测试工程师": 2.3,
        "DevOps": 0.8,
        "产品经理": 1.7,
        "项目经理": 0.8,
        "数据工程师": 3.1,
    }
    for req in REQUIREMENTS:
        month_label = req[8].strftime("%Y-%m")
        req_by_month_team[(month_label, req[3])] += req[18]

    rows = []
    capacity_id = 700001
    for month in month_starts():
        month_label = month.strftime("%Y-%m")
        for team in TEAMS:
            team_id = team[0]
            team_employees = [e for e in EMPLOYEES if e[2] == team_id]
            for role_name in ROLE_CAPACITY:
                role_employees = [e for e in team_employees if e[5] == role_name]
                if not role_employees:
                    continue
                headcount = len(role_employees)
                available = sum(e[9] for e in role_employees) * RNG.uniform(0.90, 0.99)
                story_points = req_by_month_team[(month_label, team_id)]
                role_demand = story_points * role_coeff[role_name] * RNG.uniform(0.82, 1.22)
                baseline = available * RNG.uniform(0.58, 0.92)
                demand = max(baseline, role_demand)
                if team_id in {2, 5} and role_name in {"后端开发", "测试工程师"}:
                    demand *= RNG.uniform(1.08, 1.22)
                assigned = min(available * RNG.uniform(0.88, 0.98), demand * RNG.uniform(0.86, 1.03))
                rows.append(
                    (
                        capacity_id,
                        month_label,
                        team_id,
                        role_name,
                        headcount,
                        round(available, 1),
                        round(demand, 1),
                        round(assigned, 1),
                        round(demand - available, 1),
                    )
                )
                capacity_id += 1
    return rows


CAPACITY = build_capacity()

FLOW_STAGES = ["需求池", "分析澄清", "就绪待开发", "开发中", "代码评审", "测试验证", "待发布", "已上线"]
ACTIVE_FLOW_STAGES = FLOW_STAGES[:-1]
FLOW_STAGE_ORDER = {stage: index + 1 for index, stage in enumerate(FLOW_STAGES)}
FLOW_DONE_STAGE = FLOW_STAGES[-1]
STATUS_TO_FLOW_STAGE = {
    "需求池": "需求池",
    "分析澄清": "分析澄清",
    "就绪待开发": "就绪待开发",
    "开发中": "开发中",
    "代码评审": "代码评审",
    "测试验证": "测试验证",
    "待发布": "待发布",
    "已上线": FLOW_DONE_STAGE,
    "暂缓": "需求池",
}
STAGE_SLE_DAYS = {
    "需求池": 5,
    "分析澄清": 7,
    "就绪待开发": 6,
    "开发中": 12,
    "代码评审": 4,
    "测试验证": 8,
    "待发布": 5,
    FLOW_DONE_STAGE: 9999,
}


def build_stage_sle_case_sql(alias="s", age_field="age_days"):
    lines = ["CASE"]
    for stage, days in STAGE_SLE_DAYS.items():
        if stage == FLOW_DONE_STAGE:
            continue
        lines.append(
            f"        WHEN {alias}.stage_name = {sql_literal(stage)} "
            f"AND {alias}.{age_field} > {days} THEN 1"
        )
    lines.append("        ELSE 0")
    lines.append("      END")
    return "\n".join(lines)


STAGE_SLE_CASE_SQL = build_stage_sle_case_sql()
STAGE_DURATION_SLE_CASE_SQL = build_stage_sle_case_sql("e", "duration_days")
STAGE_ORDER_CASE_SQL = "CASE " + " ".join(
    f"WHEN e.stage_name = {sql_literal(stage)} THEN {order} "
    for stage, order in FLOW_STAGE_ORDER.items()
) + "ELSE 999 END"
FLOW_DONE_STAGE_SQL = sql_literal(FLOW_DONE_STAGE)


def daterange(start, end):
    day = start
    while day <= end:
        yield day
        day += timedelta(days=1)


def add_stage_event(events, event_id, req_id, stage, entered, left):
    if left is not None and left < entered:
        left = entered
    duration_days = ((left or TODAY) - entered).days
    events.append(
        (
            event_id,
            req_id,
            stage,
            FLOW_STAGE_ORDER[stage],
            entered,
            left,
            max(duration_days, 0),
        )
    )
    return event_id + 1


def bounded_int(value, lower, upper):
    return max(lower, min(upper, int(round(value))))


def stage_duration_profile(req, online):
    req_id = req[0]
    team_id = req[3]
    req_type = req[5]
    priority = req[6]
    created = req[8]
    planned_online = req[9]
    discovery_days = int(req[12])
    design_days = int(req[13])
    dev_days = int(req[14])
    test_days = int(req[15])
    waiting_days = int(req[16])
    rework_count = int(req[17])
    story_points = int(req[18])

    priority_factor = {"P0": -2, "P1": -1, "P2": 0, "P3": 1}.get(priority, 0)
    complexity = max(0, story_points - 5) / 3
    rework = rework_count * 1.7
    data_or_compliance = 1 if req_type in {"数据需求", "合规安全"} else 0
    defect_fast_track = -1 if req_type == "缺陷修复" and priority in {"P0", "P1"} else 0
    team_queue_bias = {1: 3, 2: 1, 3: 5, 4: 2, 5: 0, 6: 4}.get(team_id, 2)
    calendar_wave = 4 if created.month in {3, 4} and team_id in {3, 6} else 0
    release_gap = abs(((online or planned_online) - planned_online).days) + (req_id % 4)

    return {
        "需求池": bounded_int(discovery_days + (req_id % 3) + max(priority_factor, -1), 1, 9),
        "分析澄清": bounded_int(design_days + data_or_compliance * 3 + (req_id % 4) + defect_fast_track, 1, 13),
        "就绪待开发": bounded_int(waiting_days + team_queue_bias + calendar_wave + (req_id % 5) - 2, 0, 22),
        "开发中": bounded_int(dev_days + complexity + rework + calendar_wave, 3, 32),
        "代码评审": bounded_int(2 + rework_count + (1 if story_points >= 8 else 0) + (req_id % 3), 1, 10),
        "测试验证": bounded_int(test_days + rework + data_or_compliance * 2 + (2 if team_id == 6 else 0), 2, 22),
        "待发布": bounded_int(1 + release_gap + (3 if created.weekday() >= 3 else 0), 1, 16),
    }


def scale_duration_profile(durations, available_days):
    total = sum(max(value, 0) for value in durations.values())
    if available_days <= 0 or total <= 0:
        return {stage: 1 for stage in durations}
    scaled = {}
    remaining = available_days
    stages = list(durations)
    for index, stage in enumerate(stages):
        if index == len(stages) - 1:
            scaled[stage] = max(1, remaining)
            break
        value = max(1, round(durations[stage] / total * available_days))
        value = min(value, max(1, remaining - (len(stages) - index - 1)))
        scaled[stage] = value
        remaining -= value
    return scaled


def build_stage_events_and_snapshots():
    events = []
    snapshots = []
    event_id = 800001
    snapshot_id = 810001
    for req in REQUIREMENTS:
        req_id = req[0]
        status = req[7]
        created = req[8]
        online = req[10]
        delivered = bool(req[20])
        durations = stage_duration_profile(req, online)

        cursor = created
        if delivered and online:
            available_days = max(1, (online - created).days)
            delivered_durations = scale_duration_profile(durations, available_days)
            for stage in FLOW_STAGES[:-1]:
                if cursor >= online:
                    break
                left = min(cursor + timedelta(days=delivered_durations[stage]), online)
                event_id = add_stage_event(events, event_id, req_id, stage, cursor, left)
                cursor = left
            event_id = add_stage_event(events, event_id, req_id, FLOW_DONE_STAGE, online, None)
        else:
            current_stage = STATUS_TO_FLOW_STAGE.get(status, "需求池")
            for stage in FLOW_STAGES[:-1]:
                if stage == current_stage:
                    if cursor > TODAY:
                        cursor = TODAY
                    event_id = add_stage_event(events, event_id, req_id, stage, cursor, None)
                    break
                left = cursor + timedelta(days=durations[stage])
                if left > TODAY:
                    event_id = add_stage_event(events, event_id, req_id, stage, cursor, None)
                    break
                event_id = add_stage_event(events, event_id, req_id, stage, cursor, left)
                cursor = left

    for event in events:
        _, req_id, stage, stage_order, entered, left, _duration_days = event
        last_day = min(TODAY, (left - timedelta(days=1)) if left else TODAY)
        if last_day < entered:
            continue
        for snapshot_date in daterange(max(entered, START), last_day):
            snapshots.append(
                (
                    snapshot_id,
                    snapshot_date,
                    req_id,
                    stage,
                    stage_order,
                    entered,
                    (snapshot_date - entered).days,
                    1 if stage == FLOW_DONE_STAGE else 0,
                )
            )
            snapshot_id += 1
    return events, snapshots


STAGE_EVENTS, REQUIREMENT_SNAPSHOTS = build_stage_events_and_snapshots()


def build_deployments():
    rows = []
    deployment_id = 820001
    for req in REQUIREMENTS:
        if not req[20] or req[10] is None:
            continue
        failed = RNG.random() < (0.08 + min(req[17], 3) * 0.035)
        rework_deploy = failed and RNG.random() < 0.55
        emergency = req[6] == "P0" and RNG.random() < 0.22
        recovery_hours = round(RNG.uniform(1.5, 18.0) if failed else 0, 1)
        rows.append(
            (
                deployment_id,
                req[0],
                req[2],
                req[3],
                req[10],
                "生产",
                "失败后恢复" if failed else "成功",
                round((req[14] + req[15] + 2) * 24 * RNG.uniform(0.85, 1.15), 1),
                1 if failed else 0,
                recovery_hours,
                1 if rework_deploy else 0,
                1 if emergency else 0,
                RNG.randint(1, 9),
            )
        )
        deployment_id += 1
    return rows


DEPLOYMENTS = build_deployments()
DEPLOYMENT_BY_REQ = {row[1]: row for row in DEPLOYMENTS}


def build_defects():
    rows = []
    defect_id = 830001
    severities = ["S1", "S2", "S3", "S4"]
    for req in REQUIREMENTS:
        test_defects = RNG.choices([0, 1, 2, 3, 4], weights=[32, 34, 20, 10, 4])[0]
        if req[18] >= 13:
            test_defects += RNG.choice([0, 1])
        if req[17] >= 2:
            test_defects += 1
        for _ in range(test_defects):
            found = req[8] + timedelta(days=req[12] + req[13] + req[14] + RNG.randint(0, max(req[15], 1)))
            found = min(found, TODAY)
            resolved = min(TODAY, found + timedelta(days=RNG.randint(1, 9)))
            rows.append(
                (
                    defect_id,
                    req[0],
                    req[2],
                    req[3],
                    req[4],
                    "系统测试",
                    RNG.choices(severities, weights=[5, 18, 52, 25])[0],
                    found,
                    resolved,
                    0,
                )
            )
            defect_id += 1

        prod_defects = 0
        if req[20] and req[10] is not None:
            prod_defects = RNG.choices([0, 1, 2], weights=[82, 15, 3])[0]
            if req[17] >= 2 and RNG.random() < 0.35:
                prod_defects += 1
        for _ in range(prod_defects):
            found = min(TODAY, req[10] + timedelta(days=RNG.randint(1, 30)))
            resolved = min(TODAY, found + timedelta(days=RNG.randint(1, 12)))
            rows.append(
                (
                    defect_id,
                    req[0],
                    req[2],
                    req[3],
                    req[4],
                    "生产",
                    RNG.choices(severities, weights=[8, 24, 48, 20])[0],
                    found,
                    resolved,
                    1,
                )
            )
            defect_id += 1
    return rows


DEFECTS = build_defects()


def build_incidents():
    rows = []
    incident_id = 840001
    root_causes = ["需求理解偏差", "发布配置", "代码缺陷", "测试覆盖不足", "数据质量", "容量不足"]
    for defect in DEFECTS:
        if defect[9] != 1:
            continue
        severity = "P1" if defect[6] == "S1" else ("P2" if defect[6] == "S2" else "P3")
        recovery_hours = round(RNG.uniform(1.0, 8.0) if severity == "P3" else RNG.uniform(6.0, 36.0), 1)
        deployment = DEPLOYMENT_BY_REQ.get(defect[1])
        rows.append(
            (
                incident_id,
                defect[0],
                deployment[0] if deployment else None,
                defect[2],
                defect[3],
                defect[7],
                severity,
                recovery_hours,
                RNG.choice(root_causes),
            )
        )
        incident_id += 1
    return rows


INCIDENTS = build_incidents()


def build_budget_rows():
    rows = []
    budget_id = 850001
    for month in month_starts():
        month_label = month.strftime("%Y-%m")
        for project in PROJECTS:
            project_id, _code, _name, team_id, _domain, priority, _owner = project
            priority_coeff = {"P0": 1.35, "P1": 1.12, "P2": 0.92, "P3": 0.72}.get(priority, 1)
            for category, base in [("资本化", 230000), ("费用化", 110000), ("外包服务", 150000)]:
                approved = base * priority_coeff * RNG.uniform(0.82, 1.25)
                actual = approved * RNG.uniform(0.72, 1.16)
                rows.append(
                    (
                        budget_id,
                        month_label,
                        project_id,
                        team_id,
                        category,
                        round(approved, 1),
                        round(actual, 1),
                    )
                )
                budget_id += 1
    return rows


BUDGET_ROWS = build_budget_rows()


METRIC_DEFINITIONS = [
    (860001, "FLOW_THROUGHPUT", "需求流动", "需求吞吐量", "需求管理/发布系统", "统计周期内完成并上线的需求数", "周/月/迭代", "Kanban Flow Metrics", "每日", "看团队与系统趋势，不用于个人排名"),
    (860002, "FLOW_LEAD_TIME", "需求流动", "需求交付时长", "需求状态流转", "actual_go_live_at - created_at，展示P50/P85/P95", "需求", "Kanban Flow Metrics / ISO 29148", "每日", "必须基于状态历史"),
    (860003, "FLOW_CFD", "需求流动", "部署需求累积流", "需求每日快照", "每日23:59每个需求所处阶段的存量堆叠", "日/阶段", "Kanban Flow Metrics", "每日", "每个需求每天只能落入一个阶段"),
    (860004, "CAPACITY_RATIO", "人力容量", "容量需求比", "HR/工时/需求估算", "可用容量工时 / 需求容量工时 * 100%", "月/角色/团队", "SPACE / 价值流管理", "每日", "需求工时需按复杂度或历史中位数估算"),
    (860005, "VENDOR_SLA", "供应商", "供应商SLA达成率", "供应商/需求/发布", "SLA达标需求数 / SLA适用需求数 * 100%", "月/供应商", "供应商治理", "每日", "需区分响应SLA、交付SLA、修复SLA"),
    (860006, "DORA_DF", "工程交付", "部署频率", "CI/CD/发布系统", "统计周期内生产部署次数", "月/系统", "DORA", "实时/每日", "按生产环境部署统计"),
    (860007, "DORA_LT", "工程交付", "变更前置时间", "Git/CI/CD/发布系统", "代码进入主干到生产部署的时长", "部署", "DORA", "实时/每日", "当前demo用需求开发与测试时长模拟"),
    (860008, "DORA_CFR", "质量稳定性", "变更失败率", "CI/CD/ITSM", "失败或需人工干预的生产部署数 / 生产部署数 * 100%", "月/系统", "DORA", "实时/每日", "失败定义需在发布治理中固化"),
    (860009, "QUALITY_TEST_DENSITY", "质量稳定性", "测试缺陷密度", "缺陷系统/需求系统", "系统测试缺陷数 / 迭代内需求数", "迭代/系统", "ISO 25010", "每日", "有功能点时优先按规模归一"),
    (860010, "QUALITY_PROD_DENSITY", "质量稳定性", "生产问题密度", "ITSM/发布/需求系统", "上线30天内生产问题数 / 上线需求数", "月/系统", "ISO 25010 / DORA", "每日", "需关联发布批次和需求"),
    (860011, "WORK_VALUE_TYPE", "工程活动", "工时价值分类", "工时系统/任务类型字典", "按增值、运营性增值、不增值、返工分类汇总", "日/人员/项目", "Lean Value Stream", "每日", "只解释投入结构，不评价个人产出"),
    (860012, "CODE_CHURN", "工程活动", "代码变更规模", "Git", "新增行、删除行、净行、提交数，排除生成文件", "日/仓库/人员", "SPACE Activity", "实时/每日", "代码行不是生产力，只作为活动信号"),
    (860013, "BUDGET_EXECUTION", "经营管理", "预算执行率", "预算/采购/工时", "实际执行金额 / 核定预算金额 * 100%", "月/项目/科目", "科技治理/财务治理", "每日", "资本化和费用化需分开看"),
]


TYPE_META = {
    "varchar": (0, 0, 128, 0),
    "date": (0, 0, 32, 0),
    "bigint": (2, 2, 19, 0),
    "decimal": (3, 3, 18, 2),
}


DATASETS = [
    {
        "group_id": 982001,
        "table_id": 983001,
        "name": "研发核心指标",
        "view": "v_eng_kpi",
        "fields": [
            ("delivered_requirements_mtd", "本月交付需求", "bigint", "q"),
            ("active_requirements", "活跃需求池", "bigint", "q"),
            ("capacity_demand_ratio_pct", "容量需求匹配度", "decimal", "q"),
            ("work_hours_mtd", "本月投入工时", "decimal", "q"),
            ("net_lines_mtd", "代码净行数", "bigint", "q"),
            ("vendor_sla_rate_pct", "供应商SLA达成率", "decimal", "q"),
        ],
    },
    {
        "group_id": 982002,
        "table_id": 983002,
        "name": "月度吞吐量统计",
        "view": "v_eng_throughput_monthly",
        "fields": [
            ("month_label", "月份", "varchar", "d"),
            ("department", "部门", "varchar", "d"),
            ("project_name", "项目", "varchar", "d"),
            ("submitted_count", "进入需求数", "bigint", "q"),
            ("delivered_count", "交付需求数", "bigint", "q"),
            ("story_points", "故事点", "bigint", "q"),
            ("avg_lead_time_days", "平均交付周期", "decimal", "q"),
        ],
    },
    {
        "group_id": 982003,
        "table_id": 983003,
        "name": "人力容量需求比",
        "view": "v_eng_capacity_demand",
        "fields": [
            ("month_label", "月份", "varchar", "d"),
            ("department", "部门", "varchar", "d"),
            ("role_name", "角色", "varchar", "d"),
            ("headcount", "人数", "bigint", "q"),
            ("capacity_hours", "可用容量工时", "decimal", "q"),
            ("demand_hours", "需求工时", "decimal", "q"),
            ("assigned_hours", "已排工时", "decimal", "q"),
            ("gap_hours", "缺口工时", "decimal", "q"),
            ("capacity_demand_ratio_pct", "容量需求比", "decimal", "q"),
        ],
    },
    {
        "group_id": 982004,
        "table_id": 983004,
        "name": "角色分布透视",
        "view": "v_eng_role_distribution",
        "fields": [
            ("department", "部门", "varchar", "d"),
            ("role_name", "角色", "varchar", "d"),
            ("employment_type", "用工类型", "varchar", "d"),
            ("vendor_name", "供应商", "varchar", "d"),
            ("headcount", "人数", "bigint", "q"),
            ("capacity_hours", "月可用工时", "decimal", "q"),
        ],
    },
    {
        "group_id": 982005,
        "table_id": 983005,
        "name": "供应商管理分析",
        "view": "v_eng_vendor_management",
        "fields": [
            ("vendor_name", "供应商", "varchar", "d"),
            ("vendor_type", "供应商类型", "varchar", "d"),
            ("sla_level", "SLA等级", "varchar", "d"),
            ("headcount", "人数", "bigint", "q"),
            ("delivered_count", "交付需求数", "bigint", "q"),
            ("avg_lead_time_days", "平均交付周期", "decimal", "q"),
            ("sla_hit_rate_pct", "SLA达成率", "decimal", "q"),
            ("billable_hours", "结算工时", "decimal", "q"),
            ("cost_amount", "预估成本", "decimal", "q"),
            ("net_lines", "净代码行", "bigint", "q"),
        ],
    },
    {
        "group_id": 982006,
        "table_id": 983006,
        "name": "需求分段时效",
        "view": "v_eng_stage_aging",
        "fields": [
            ("stage_name", "需求阶段", "varchar", "d"),
            ("priority", "优先级", "varchar", "d"),
            ("requirement_type", "需求类型", "varchar", "d"),
            ("req_count", "需求数", "bigint", "q"),
            ("avg_days", "平均耗时", "decimal", "q"),
            ("p90_days", "P90耗时", "decimal", "q"),
        ],
    },
    {
        "group_id": 982007,
        "table_id": 983007,
        "name": "部署需求累积流",
        "view": "v_eng_requirement_flow",
        "fields": [
            ("stat_date", "日期", "varchar", "d"),
            ("stage_name", "需求阶段", "varchar", "d"),
            ("department", "部门", "varchar", "d"),
            ("project_name", "项目", "varchar", "d"),
            ("requirement_code", "需求编码", "varchar", "d"),
            ("priority", "优先级", "varchar", "d"),
            ("requirement_type", "需求类型", "varchar", "d"),
            ("owner_name", "负责人", "varchar", "d"),
            ("age_days", "在库天数", "bigint", "q"),
            ("demand_hours", "需求工时", "decimal", "q"),
            ("req_count", "需求数", "bigint", "q"),
        ],
    },
    {
        "group_id": 982019,
        "table_id": 983019,
        "name": "部署需求活跃累积流",
        "view": "v_eng_requirement_active_flow",
        "fields": [
            ("stat_date", "日期", "varchar", "d"),
            ("stage_name", "需求阶段", "varchar", "d"),
            ("department", "部门", "varchar", "d"),
            ("project_name", "项目", "varchar", "d"),
            ("requirement_code", "需求编码", "varchar", "d"),
            ("priority", "优先级", "varchar", "d"),
            ("requirement_type", "需求类型", "varchar", "d"),
            ("owner_name", "负责人", "varchar", "d"),
            ("age_days", "在库天数", "bigint", "q"),
            ("demand_hours", "需求工时", "decimal", "q"),
            ("req_count", "需求数", "bigint", "q"),
        ],
    },
    {
        "group_id": 982008,
        "table_id": 983008,
        "name": "工时代码实时统计",
        "view": "v_eng_work_code_daily",
        "fields": [
            ("stat_date", "日期", "varchar", "d"),
            ("department", "部门", "varchar", "d"),
            ("project_name", "项目", "varchar", "d"),
            ("person_name", "人员", "varchar", "d"),
            ("role_name", "角色", "varchar", "d"),
            ("work_type", "分类", "varchar", "d"),
            ("total_hours", "工时", "decimal", "q"),
            ("commit_count", "提交数", "bigint", "q"),
            ("net_lines", "净代码行", "bigint", "q"),
        ],
    },
    {
        "group_id": 982009,
        "table_id": 983009,
        "name": "工时分类分析",
        "view": "v_eng_work_type_summary",
        "fields": [
            ("work_type", "分类", "varchar", "d"),
            ("department", "部门", "varchar", "d"),
            ("total_hours", "工时", "decimal", "q"),
            ("commit_count", "提交数", "bigint", "q"),
            ("net_lines", "净代码行", "bigint", "q"),
        ],
    },
    {
        "group_id": 982010,
        "table_id": 983010,
        "name": "项目下钻明细",
        "view": "v_eng_project_drill",
        "fields": [
            ("department", "部门", "varchar", "d"),
            ("project_code", "项目编码", "varchar", "d"),
            ("project_name", "项目", "varchar", "d"),
            ("priority", "项目优先级", "varchar", "d"),
            ("product_owner", "负责人", "varchar", "d"),
            ("active_reqs", "活跃需求", "bigint", "q"),
            ("delivered_reqs", "已交付需求", "bigint", "q"),
            ("demand_hours", "需求工时", "decimal", "q"),
            ("work_hours", "投入工时", "decimal", "q"),
            ("net_lines", "净代码行", "bigint", "q"),
            ("avg_lead_time_days", "平均交付周期", "decimal", "q"),
            ("risk_level", "风险等级", "varchar", "d"),
        ],
    },
    {
        "group_id": 982011,
        "table_id": 983011,
        "name": "人员工时代码明细",
        "view": "v_eng_person_drill",
        "fields": [
            ("department", "部门", "varchar", "d"),
            ("person_name", "人员", "varchar", "d"),
            ("role_name", "角色", "varchar", "d"),
            ("employment_type", "用工类型", "varchar", "d"),
            ("vendor_name", "供应商", "varchar", "d"),
            ("work_hours", "投入工时", "decimal", "q"),
            ("commit_count", "提交数", "bigint", "q"),
            ("net_lines", "净代码行", "bigint", "q"),
            ("requirement_count", "关联需求数", "bigint", "q"),
            ("utilization_rate_pct", "利用率", "decimal", "q"),
        ],
    },
    {
        "group_id": 982012,
        "table_id": 983012,
        "name": "指标口径与数据来源",
        "view": "v_eng_metric_catalog",
        "fields": [
            ("domain_name", "指标域", "varchar", "d"),
            ("metric_name", "指标名称", "varchar", "d"),
            ("source_system", "来源系统", "varchar", "d"),
            ("formula_text", "计算口径", "varchar", "d"),
            ("grain", "统计粒度", "varchar", "d"),
            ("standard_basis", "参考框架", "varchar", "d"),
            ("refresh_frequency", "刷新频率", "varchar", "d"),
            ("caveat", "使用注意", "varchar", "d"),
        ],
    },
    {
        "group_id": 982013,
        "table_id": 983013,
        "name": "DORA交付能力",
        "view": "v_eng_dora_metrics",
        "fields": [
            ("month_label", "月份", "varchar", "d"),
            ("department", "部门", "varchar", "d"),
            ("project_name", "系统/项目", "varchar", "d"),
            ("deployment_count", "部署次数", "bigint", "q"),
            ("avg_change_lead_time_days", "变更前置时间", "decimal", "q"),
            ("change_failure_rate_pct", "变更失败率", "decimal", "q"),
            ("avg_recovery_time_hours", "恢复时间", "decimal", "q"),
            ("rework_deployment_rate_pct", "部署返工率", "decimal", "q"),
        ],
    },
    {
        "group_id": 982014,
        "table_id": 983014,
        "name": "质量风险预警",
        "view": "v_eng_quality_risk",
        "fields": [
            ("department", "部门", "varchar", "d"),
            ("project_name", "系统/项目", "varchar", "d"),
            ("delivered_reqs", "上线需求数", "bigint", "q"),
            ("test_defect_density", "测试缺陷密度", "decimal", "q"),
            ("production_problem_density", "生产问题密度", "decimal", "q"),
            ("escaped_defects", "生产缺陷数", "bigint", "q"),
            ("p1_p2_incidents", "P1/P2事件", "bigint", "q"),
            ("avg_recovery_time_hours", "平均恢复时间", "decimal", "q"),
            ("risk_level", "风险等级", "varchar", "d"),
        ],
    },
    {
        "group_id": 982015,
        "table_id": 983015,
        "name": "预算执行分析",
        "view": "v_eng_budget_execution",
        "fields": [
            ("month_label", "月份", "varchar", "d"),
            ("department", "部门", "varchar", "d"),
            ("project_name", "项目", "varchar", "d"),
            ("budget_category", "预算类别", "varchar", "d"),
            ("approved_amount", "核定预算", "decimal", "q"),
            ("actual_amount", "实际执行", "decimal", "q"),
            ("execution_rate_pct", "执行率", "decimal", "q"),
        ],
    },
    {
        "group_id": 982016,
        "table_id": 983016,
        "name": "需求漏斗与WIP",
        "view": "v_eng_demand_funnel",
        "fields": [
            ("stage_name", "需求阶段", "varchar", "d"),
            ("department", "部门", "varchar", "d"),
            ("project_name", "项目", "varchar", "d"),
            ("priority", "优先级", "varchar", "d"),
            ("req_count", "需求数", "bigint", "q"),
            ("avg_age_days", "平均在库天数", "decimal", "q"),
            ("over_sle_count", "SLE超期数", "bigint", "q"),
        ],
    },
    {
        "group_id": 982017,
        "table_id": 983017,
        "name": "阶段周期漏斗",
        "view": "v_eng_stage_funnel",
        "fields": [
            ("stage_name", "阶段", "varchar", "d"),
            ("stage_order", "阶段顺序", "bigint", "q"),
            ("period_label", "时间周期", "varchar", "d"),
            ("department", "部门", "varchar", "d"),
            ("project_name", "项目", "varchar", "d"),
            ("req_count", "需求数", "bigint", "q"),
            ("avg_days", "平均耗时天", "decimal", "q"),
            ("p90_days", "P90耗时天", "decimal", "q"),
            ("over_sle_count", "SLE超期数", "bigint", "q"),
        ],
    },
    {
        "group_id": 982018,
        "table_id": 983018,
        "name": "投入产出效率矩阵",
        "view": "v_eng_efficiency_matrix",
        "fields": [
            ("department", "部门", "varchar", "d"),
            ("team_name", "团队", "varchar", "d"),
            ("project_name", "项目", "varchar", "d"),
            ("efficiency_level", "效率等级", "varchar", "d"),
            ("workforce_days", "人力投入人天", "decimal", "q"),
            ("delivered_requirements", "交付需求数", "bigint", "q"),
            ("efficiency_ratio", "交付/人天", "decimal", "q"),
            ("capacity_hours", "容量工时", "decimal", "q"),
            ("demand_hours", "需求工时", "decimal", "q"),
        ],
    },
]


FIELD_MAP = {}
DATASET_BY_GROUP = {}


def make_field(dataset, index, origin_name, name, field_type, group_type):
    field_id = 984000000 + (dataset["group_id"] - 982000) * 100 + index
    de_type, de_extract_type, size, accuracy = TYPE_META[field_type]
    field = {
        "id": field_id,
        "datasourceId": DATASOURCE_ID,
        "datasetTableId": dataset["table_id"],
        "datasetGroupId": dataset["group_id"],
        "chartId": None,
        "originName": origin_name,
        "name": name,
        "dbFieldName": None,
        "description": f"研发效能演示字段：{origin_name}",
        "dataeaseName": f"f_{index}",
        "fieldShortName": f"f_{index}",
        "groupList": [],
        "otherGroup": None,
        "groupType": group_type,
        "type": field_type if field_type != "date" else "varchar",
        "size": size,
        "precision": None,
        "scale": None,
        "deType": de_type,
        "deExtractType": de_extract_type,
        "extField": 0,
        "checked": True,
        "columnIndex": index,
        "lastSyncTime": NOW_MS,
        "accuracy": accuracy,
        "dateFormat": None,
        "dateFormatType": None,
        "params": [],
        "desensitized": False,
        "orderChecked": False,
    }
    return field


def init_dataset_fields():
    for dataset in DATASETS:
        DATASET_BY_GROUP[dataset["group_id"]] = dataset
        fields = []
        for index, (origin_name, name, field_type, group_type) in enumerate(dataset["fields"], start=1):
            field = make_field(dataset, index, origin_name, name, field_type, group_type)
            fields.append(field)
            FIELD_MAP[(dataset["group_id"], origin_name)] = field
        dataset["field_objs"] = fields


init_dataset_fields()


def chart_field(group_id, origin_name, summary=None, formatter=None, chart_show_name=None):
    field = copy.deepcopy(FIELD_MAP[(group_id, origin_name)])
    if summary is None:
        summary = "sum" if field["groupType"] == "q" else "count"
    field.update(
        {
            "summary": summary,
            "sort": "none",
            "dateStyle": "y_M_d",
            "datePattern": "date_sub",
            "dateShowFormat": "y_M_d",
            "chartType": "bar",
            "compareCalc": {"type": "none", "resultData": "percent", "field": None, "custom": None},
            "logic": None,
            "filterType": None,
            "index": None,
            "formatterCfg": formatter
            or {
                "type": "auto",
                "unitLanguage": "ch",
                "unit": 1,
                "suffix": "",
                "decimalCount": 1 if field["deType"] == 3 else 0,
                "thousandSeparator": field["groupType"] == "q",
            },
            "chartShowName": chart_show_name,
            "filter": [],
            "customSort": None,
            "busiType": None,
            "hide": False,
            "field": None,
            "agg": False,
        }
    )
    return field


def percent_formatter(decimal_count=1):
    return {
        "type": "auto",
        "unitLanguage": "ch",
        "unit": 1,
        "suffix": "%",
        "decimalCount": decimal_count,
        "thousandSeparator": False,
    }


def common_attr(title, color=None, table=False, label_show=False):
    color = color or LIGHT_THEME["primary"]
    return {
        "basicStyle": {
            "colorScheme": "custom",
            "colors": [color] + [item for item in LIGHT_THEME["chart_palette"] if item != color],
            "alpha": 96,
            "gradient": True,
            "mapStyle": "light",
            "columnWidthRatio": 44,
            "lineWidth": 3,
            "lineSymbol": "circle",
            "lineSymbolSize": 4,
            "lineSmooth": True,
            "radius": 0.78,
            "innerRadius": 0.62,
            "barDefault": True,
            "barWidth": 28,
            "barGap": 0.34,
            "lineType": "solid",
            "scatterSymbol": "circle",
            "scatterSymbolSize": 10,
            "tableBorderColor": LIGHT_THEME["border"],
            "tableScrollBarColor": LIGHT_THEME["scrollbar"],
            "tableColumnMode": "adapt",
            "tableColumnWidth": 100,
            "tablePageMode": "pull",
            "tablePageSize": 20,
            "showSummary": False,
            "summaryLabel": "合计",
            "maxLines": 2,
            "gaugeStyle": "default",
            "areaBorderColor": LIGHT_THEME["area_border"],
            "areaBaseColor": LIGHT_THEME["surface_alt"],
            "seriesColor": [],
            "showHoverStyle": True,
            "autoWrap": False,
        },
        "misc": {
            "flowMapConfig": {
                "lineConfig": {
                    "mapLineAnimate": True,
                    "mapLineGradient": False,
                    "mapLineSourceColor": color,
                    "mapLineTargetColor": LIGHT_THEME["teal"],
                }
            },
            "nameFontColor": LIGHT_THEME["text"],
            "valueFontColor": color,
            "pieInnerRadius": 62,
            "pieOuterRadius": 84,
            "gaugeMinType": "fix",
            "gaugeMinField": {"id": "", "summary": ""},
            "gaugeMin": 0,
            "gaugeMaxType": "fix",
            "gaugeMaxField": {"id": "", "summary": ""},
            "gaugeMax": 100,
            "gaugeStartAngle": 225,
            "gaugeEndAngle": -45,
            "liquidMax": 100,
            "liquidMaxType": "fix",
            "liquidMaxField": {"id": "", "summary": ""},
            "liquidSize": 78,
            "liquidShape": "circle",
            "hPosition": "center",
            "vPosition": "center",
        },
        "label": {
            "show": label_show,
            "color": LIGHT_THEME["text"],
            "fontSize": 12,
            "position": "top",
            "fullDisplay": False,
            "formatter": "",
            "labelLine": {"show": True},
            "labelFormatter": {
                "type": "auto",
                "unitLanguage": "ch",
                "unit": 1,
                "suffix": "",
                "decimalCount": 1,
                "thousandSeparator": False,
            },
            "reserveDecimalCount": 2,
            "showDimension": True,
            "showQuota": False,
            "showProportion": True,
            "conversionTag": True,
            "seriesLabelFormatter": [],
        },
        "tooltip": {
            "show": True,
            "trigger": "item",
            "confine": True,
            "color": LIGHT_THEME["text"],
            "fontSize": 12,
            "tooltipFormatter": {
                "type": "auto",
                "unitLanguage": "ch",
                "unit": 1,
                "suffix": "",
                "decimalCount": 2,
                "thousandSeparator": True,
            },
            "backgroundColor": LIGHT_THEME["tooltip_bg"],
            "seriesTooltipFormatter": [],
            "carousel": {"enable": False, "time": 2},
        },
        "tableHeader": {
            "indexLabel": "序号",
            "showIndex": table,
            "showTableHeader": True,
            "tableHeaderAlign": "left",
            "tableHeaderBgColor": LIGHT_THEME["soft_blue"],
            "tableHeaderFontColor": LIGHT_THEME["text"],
            "tableHeaderCornerBgColor": LIGHT_THEME["soft_blue"],
            "tableHeaderCornerFontColor": LIGHT_THEME["text"],
            "tableHeaderColBgColor": LIGHT_THEME["soft_blue"],
            "tableHeaderColFontColor": LIGHT_THEME["text"],
            "tableTitleFontSize": 13,
            "tableTitleHeight": 32,
            "tableHeaderSort": True,
            "headerGroup": False,
        },
        "tableCell": {
            "tableItemBgColor": LIGHT_THEME["surface"],
            "tableFontColor": LIGHT_THEME["text"],
            "tableItemSubBgColor": LIGHT_THEME["surface_alt"],
            "tableItemAlign": "right",
            "tableItemFontSize": 12,
            "tableItemHeight": 30,
            "enableTableCrossBG": True,
            "showTooltip": True,
            "showHorizonBorder": True,
            "showVerticalBorder": True,
            "tableFreeze": False,
            "tableColumnFreezeHead": 0,
            "tableRowFreezeHead": 0,
            "mergeCells": False,
        },
        "tableTotal": {},
        "indicator": {
            "fontSize": 34,
            "color": color,
            "hPosition": "center",
            "isItalic": False,
            "isBolder": True,
            "fontFamily": "PingFang SC",
            "letterSpace": "0",
            "fontShadow": True,
        },
        "indicatorName": {
            "title": title,
            "fontSize": 13,
            "color": LIGHT_THEME["muted"],
            "hPosition": "center",
            "isItalic": False,
            "isBolder": False,
            "fontFamily": "PingFang SC",
            "letterSpace": "0",
            "fontShadow": False,
        },
        "map": {"id": "", "level": "world"},
    }


def common_style(title, table=False):
    axis_line = LIGHT_THEME["axis_line"]
    return {
        "text": {
            "show": True,
            "title": title,
            "fontSize": 15 if not table else 14,
            "color": LIGHT_THEME["text"],
            "hPosition": "left",
            "vPosition": "top",
            "isItalic": False,
            "isBolder": True,
            "remarkShow": False,
            "remark": "",
            "fontFamily": "PingFang SC",
            "letterSpace": "0",
            "fontShadow": False,
            "remarkBackgroundColor": LIGHT_THEME["surface"],
        },
        "legend": {
            "show": not table,
            "fontSize": 11,
            "color": LIGHT_THEME["muted"],
            "orient": "horizontal",
            "hPosition": "right",
            "vPosition": "top",
            "icon": "circle",
        },
        "xAxis": {
            "show": True,
            "position": "bottom",
            "name": "",
            "color": LIGHT_THEME["muted"],
            "fontSize": 12,
            "axisLabel": {
                "show": True,
                "color": LIGHT_THEME["muted"],
                "fontSize": 11,
                "rotate": 0,
                "formatter": "{value}",
                "lengthLimit": 9,
            },
            "axisLine": {
                "show": True,
                "color": axis_line,
                "lineStyle": {"color": axis_line, "width": 1, "style": "solid"},
            },
            "splitLine": {
                "show": False,
                "color": LIGHT_THEME["grid"],
                "lineStyle": {"color": LIGHT_THEME["grid"], "width": 1, "style": "solid"},
            },
            "axisValue": {"auto": True, "min": None, "max": None, "split": None, "splitCount": None},
            "axisLabelFormatter": {
                "type": "auto",
                "unitLanguage": "ch",
                "unit": 1,
                "suffix": "",
                "decimalCount": 0,
                "thousandSeparator": False,
            },
        },
        "yAxis": {
            "show": True,
            "position": "left",
            "name": "",
            "color": LIGHT_THEME["muted"],
            "fontSize": 12,
            "axisLabel": {"show": True, "color": LIGHT_THEME["muted"], "fontSize": 11, "rotate": 0, "formatter": "{value}"},
            "axisLine": {
                "show": False,
                "color": axis_line,
                "lineStyle": {"color": axis_line, "width": 1, "style": "solid"},
            },
            "splitLine": {
                "show": True,
                "color": LIGHT_THEME["grid"],
                "lineStyle": {"color": LIGHT_THEME["grid"], "width": 1, "style": "solid"},
            },
            "axisValue": {"auto": True, "min": None, "max": None, "split": None, "splitCount": None},
            "axisLabelFormatter": {
                "type": "auto",
                "unitLanguage": "ch",
                "unit": 1,
                "suffix": "",
                "decimalCount": 0,
                "thousandSeparator": True,
            },
        },
        "yAxisExt": {
            "show": True,
            "position": "right",
            "name": "",
            "color": LIGHT_THEME["muted"],
            "fontSize": 12,
            "axisLabel": {"show": True, "color": LIGHT_THEME["muted"], "fontSize": 11, "rotate": 0, "formatter": "{value}"},
            "axisLine": {"show": False},
            "splitLine": {"show": False},
            "axisValue": {"auto": True, "min": None, "max": None, "split": None, "splitCount": None},
            "axisLabelFormatter": {
                "type": "auto",
                "unitLanguage": "ch",
                "unit": 1,
                "suffix": "",
                "decimalCount": 0,
                "thousandSeparator": True,
            },
        },
        "misc": {
            "padding": {"top": 32 if not table else 28, "right": 14, "bottom": 16, "left": 16},
            "showName": False,
            "color": LIGHT_THEME["text"],
            "fontSize": 12,
            "axisColor": LIGHT_THEME["muted"],
            "splitNumber": 5,
            "axisLine": {"show": True, "lineStyle": {"color": LIGHT_THEME["muted"], "width": 1, "type": "solid"}},
            "axisTick": {"show": False, "length": 5, "lineStyle": {"color": LIGHT_THEME["muted"], "width": 1, "type": "solid"}},
            "axisLabel": {"show": False, "rotate": 0, "margin": 8, "color": LIGHT_THEME["muted"], "fontSize": "12", "formatter": "{value}"},
            "splitLine": {"show": True, "lineStyle": {"color": LIGHT_THEME["grid"], "width": 1, "type": "solid"}},
            "splitArea": {"show": False},
        },
    }


SENIOR = {
    "functionCfg": {
        "emptyDataStrategy": "breakLine",
        "sliderShow": False,
        "sliderRange": [0, 10],
        "sliderBg": LIGHT_THEME["soft_blue"],
        "sliderFillBg": LIGHT_THEME["primary"],
        "sliderTextColor": LIGHT_THEME["muted"],
        "emptyDataFieldCtrl": [],
    },
    "assistLineCfg": {},
    "assistLine": [],
    "threshold": {
        "enable": False,
        "gaugeThreshold": "",
        "labelThreshold": [],
        "tableThreshold": [],
        "textLabelThreshold": [],
    },
    "scrollCfg": {"open": False, "row": 1, "interval": 2000, "step": 50},
    "areaMapping": {},
    "bubbleCfg": {},
}


def chart_def(
    chart_id,
    title,
    group_id,
    chart_type,
    x=None,
    y=None,
    y_ext=None,
    x_ext=None,
    stack=None,
    ext_bubble=None,
    ext_label=None,
    ext_tooltip=None,
    ext_color=None,
    drill=None,
    color=None,
    render=None,
    scene_id=SCREEN_ID,
):
    x = x or []
    y = y or []
    y_ext = y_ext or []
    x_ext = x_ext or []
    stack = stack or []
    ext_bubble = ext_bubble or []
    ext_label = ext_label or []
    ext_tooltip = ext_tooltip or []
    ext_color = ext_color or []
    drill = drill or []
    is_table = chart_type == "table-info"
    fields = []
    for field in x + y + y_ext + x_ext + stack + ext_bubble + ext_label + ext_tooltip + ext_color:
        if field["id"] not in [item["id"] for item in fields]:
            fields.append(field)
    return {
        "id": chart_id,
        "title": title,
        "scene_id": scene_id,
        "group_id": group_id,
        "type": chart_type,
        "render": render or ("custom" if chart_type == "indicator" else "antv"),
        "x_axis": x,
        "y_axis": y,
        "y_axis_ext": y_ext,
        "x_axis_ext": x_ext,
        "ext_stack": stack,
        "ext_bubble": ext_bubble,
        "ext_label": ext_label,
        "ext_tooltip": ext_tooltip,
        "ext_color": ext_color,
        "drill_fields": drill,
        "view_fields": fields,
        "custom_attr": common_attr(title, color=color, table=is_table, label_show=chart_type in {"bar-horizontal"}),
        "custom_style": common_style(title, table=is_table),
    }


def build_charts():
    pct = percent_formatter()
    active_flow_stage = chart_field(982019, "stage_name")
    active_flow_stage["sort"] = "custom_sort"
    active_flow_stage["customSort"] = ACTIVE_FLOW_STAGES
    stage_funnel_stage = chart_field(982017, "stage_name")
    stage_funnel_stage["sort"] = "custom_sort"
    stage_funnel_stage["customSort"] = ACTIVE_FLOW_STAGES
    charts = [
        chart_def(980101, "本月交付需求", 982001, "indicator", y=[chart_field(982001, "delivered_requirements_mtd")], color=LIGHT_THEME["primary"]),
        chart_def(980102, "活跃需求池", 982001, "indicator", y=[chart_field(982001, "active_requirements")], color=LIGHT_THEME["teal"]),
        chart_def(980103, "容量需求匹配度", 982001, "indicator", y=[chart_field(982001, "capacity_demand_ratio_pct", formatter=pct)], color=LIGHT_THEME["accent"]),
        chart_def(980104, "本月投入工时", 982001, "indicator", y=[chart_field(982001, "work_hours_mtd")], color=LIGHT_THEME["rose"]),
        chart_def(980105, "代码净行数", 982001, "indicator", y=[chart_field(982001, "net_lines_mtd")], color=LIGHT_THEME["secondary"]),
        chart_def(980106, "供应商SLA达成率", 982001, "indicator", y=[chart_field(982001, "vendor_sla_rate_pct", formatter=pct)], color=LIGHT_THEME["teal"]),
        chart_def(
            980107,
            "部署需求累积流图",
            982019,
            "cumulative-flow",
            x=[chart_field(982019, "stat_date")],
            y=[chart_field(982019, "req_count")],
            stack=[active_flow_stage],
            drill=[
                active_flow_stage,
                chart_field(982019, "department"),
                chart_field(982019, "project_name"),
                chart_field(982019, "priority"),
                chart_field(982019, "requirement_type"),
                chart_field(982019, "requirement_code"),
            ],
            color=LIGHT_THEME["accent"],
        ),
        chart_def(
            980108,
            "月度吞吐量趋势",
            982002,
            "line",
            x=[chart_field(982002, "month_label")],
            y=[chart_field(982002, "delivered_count"), chart_field(982002, "submitted_count")],
            stack=[chart_field(982002, "department")],
            drill=[chart_field(982002, "department"), chart_field(982002, "project_name")],
            color=LIGHT_THEME["teal"],
        ),
        chart_def(
            980109,
            "人力容量需求比",
            982003,
            "bar-group",
            x=[chart_field(982003, "department")],
            y=[chart_field(982003, "capacity_hours"), chart_field(982003, "demand_hours")],
            drill=[chart_field(982003, "department"), chart_field(982003, "role_name")],
            color=LIGHT_THEME["accent"],
        ),
        chart_def(
            980110,
            "需求分段时效",
            982006,
            "bar-horizontal",
            x=[chart_field(982006, "stage_name")],
            y=[chart_field(982006, "avg_days", summary="avg")],
            drill=[chart_field(982006, "stage_name"), chart_field(982006, "priority"), chart_field(982006, "requirement_type")],
            color=LIGHT_THEME["rose"],
        ),
        chart_def(
            980111,
            "角色分布透视",
            982004,
            "bar-group",
            x=[chart_field(982004, "department")],
            y=[chart_field(982004, "headcount")],
            x_ext=[chart_field(982004, "employment_type")],
            drill=[chart_field(982004, "department"), chart_field(982004, "role_name"), chart_field(982004, "vendor_name")],
            color=LIGHT_THEME["secondary"],
        ),
        chart_def(
            980112,
            "供应商交付质量",
            982005,
            "bar-horizontal",
            x=[chart_field(982005, "vendor_name")],
            y=[chart_field(982005, "sla_hit_rate_pct", summary="avg", formatter=pct)],
            drill=[chart_field(982005, "vendor_name"), chart_field(982005, "vendor_type")],
            color=LIGHT_THEME["teal"],
        ),
        chart_def(
            980113,
            "工时/代码行实时趋势",
            982008,
            "line",
            x=[chart_field(982008, "stat_date")],
            y=[chart_field(982008, "total_hours"), chart_field(982008, "net_lines")],
            drill=[chart_field(982008, "department"), chart_field(982008, "project_name"), chart_field(982008, "person_name")],
            color=LIGHT_THEME["primary"],
        ),
        chart_def(
            980114,
            "工时分类分析",
            982009,
            "bar-horizontal",
            x=[chart_field(982009, "work_type")],
            y=[chart_field(982009, "total_hours")],
            drill=[chart_field(982009, "work_type"), chart_field(982009, "department")],
            color=LIGHT_THEME["accent"],
        ),
        chart_def(
            980115,
            "项目下钻明细",
            982010,
            "table-info",
            x=[chart_field(982010, f[0]) for f in DATASET_BY_GROUP[982010]["fields"]],
            y=[],
            drill=[chart_field(982010, "department"), chart_field(982010, "project_name")],
            color=LIGHT_THEME["primary"],
        ),
        chart_def(
            980116,
            "人员工时/代码明细",
            982011,
            "table-info",
            x=[chart_field(982011, f[0]) for f in DATASET_BY_GROUP[982011]["fields"]],
            y=[],
            drill=[chart_field(982011, "department"), chart_field(982011, "person_name")],
            color=LIGHT_THEME["teal"],
        ),
        chart_def(
            980117,
            "DORA交付能力",
            982013,
            "bar-group",
            x=[chart_field(982013, "department")],
            y=[
                chart_field(982013, "deployment_count"),
                chart_field(982013, "change_failure_rate_pct", summary="avg", formatter=pct),
            ],
            drill=[chart_field(982013, "department"), chart_field(982013, "project_name"), chart_field(982013, "month_label")],
            color=LIGHT_THEME["primary"],
        ),
        chart_def(
            980118,
            "预算执行率",
            982015,
            "bar-group",
            x=[chart_field(982015, "department")],
            y=[chart_field(982015, "approved_amount"), chart_field(982015, "actual_amount")],
            drill=[chart_field(982015, "department"), chart_field(982015, "project_name"), chart_field(982015, "budget_category")],
            color=LIGHT_THEME["accent"],
        ),
        chart_def(
            980119,
            "指标口径与来源",
            982012,
            "table-info",
            x=[chart_field(982012, f[0]) for f in DATASET_BY_GROUP[982012]["fields"]],
            y=[],
            drill=[chart_field(982012, "domain_name"), chart_field(982012, "metric_name")],
            color=LIGHT_THEME["muted"],
        ),
        chart_def(
            980201,
            "部署需求累积流图",
            982019,
            "cumulative-flow",
            x=[chart_field(982019, "stat_date")],
            y=[chart_field(982019, "req_count")],
            stack=[copy.deepcopy(active_flow_stage)],
            drill=[
                chart_field(982019, "stage_name"),
                chart_field(982019, "department"),
                chart_field(982019, "project_name"),
                chart_field(982019, "priority"),
                chart_field(982019, "requirement_type"),
                chart_field(982019, "requirement_code"),
            ],
            color=LIGHT_THEME["accent"],
            scene_id=980002,
        ),
        chart_def(
            980202,
            "阶段周期漏斗",
            982017,
            "stage-funnel",
            x=[stage_funnel_stage],
            y=[chart_field(982017, "req_count")],
            ext_tooltip=[
                chart_field(982017, "avg_days", summary="avg"),
                chart_field(982017, "p90_days", summary="avg"),
                chart_field(982017, "over_sle_count"),
            ],
            drill=[chart_field(982017, "stage_name"), chart_field(982017, "department"), chart_field(982017, "project_name")],
            color=LIGHT_THEME["accent"],
            scene_id=980002,
        ),
        chart_def(
            980203,
            "分段时效P90",
            982006,
            "bar-horizontal",
            x=[chart_field(982006, "stage_name")],
            y=[chart_field(982006, "avg_days", summary="avg"), chart_field(982006, "p90_days", summary="avg")],
            drill=[chart_field(982006, "stage_name"), chart_field(982006, "priority"), chart_field(982006, "requirement_type")],
            color=LIGHT_THEME["rose"],
            scene_id=980002,
        ),
        chart_def(
            980204,
            "吞吐量月度趋势",
            982002,
            "line",
            x=[chart_field(982002, "month_label")],
            y=[chart_field(982002, "submitted_count"), chart_field(982002, "delivered_count")],
            stack=[chart_field(982002, "department")],
            drill=[chart_field(982002, "department"), chart_field(982002, "project_name")],
            color=LIGHT_THEME["teal"],
            scene_id=980002,
        ),
        chart_def(
            980205,
            "需求流动明细",
            982007,
            "table-info",
            x=[chart_field(982007, f[0]) for f in DATASET_BY_GROUP[982007]["fields"]],
            y=[],
            drill=[chart_field(982007, "stage_name"), chart_field(982007, "department"), chart_field(982007, "requirement_code")],
            color=LIGHT_THEME["primary"],
            scene_id=980002,
        ),
        chart_def(
            980206,
            "需求项目下钻",
            982010,
            "table-info",
            x=[chart_field(982010, f[0]) for f in DATASET_BY_GROUP[982010]["fields"]],
            y=[],
            drill=[chart_field(982010, "department"), chart_field(982010, "project_name")],
            color=LIGHT_THEME["secondary"],
            scene_id=980002,
        ),
        chart_def(
            980301,
            "人力投入-需求产出矩阵",
            982018,
            "metric-matrix",
            x=[chart_field(982018, "workforce_days", summary="avg")],
            y=[chart_field(982018, "delivered_requirements", summary="avg")],
            y_ext=[chart_field(982018, "efficiency_ratio", summary="avg")],
            ext_bubble=[chart_field(982018, "demand_hours", summary="avg")],
            ext_color=[chart_field(982018, "efficiency_level")],
            ext_label=[chart_field(982018, "project_name")],
            ext_tooltip=[
                chart_field(982018, "efficiency_ratio", summary="avg"),
                chart_field(982018, "capacity_hours", summary="avg"),
                chart_field(982018, "demand_hours", summary="avg"),
            ],
            drill=[chart_field(982018, "department"), chart_field(982018, "team_name"), chart_field(982018, "project_name"), chart_field(982018, "efficiency_level")],
            color=LIGHT_THEME["accent"],
            scene_id=980003,
        ),
        chart_def(
            980302,
            "角色/用工分布",
            982004,
            "bar-group",
            x=[chart_field(982004, "role_name")],
            y=[chart_field(982004, "headcount")],
            x_ext=[chart_field(982004, "employment_type")],
            drill=[chart_field(982004, "department"), chart_field(982004, "role_name"), chart_field(982004, "vendor_name")],
            color=LIGHT_THEME["secondary"],
            scene_id=980003,
        ),
        chart_def(
            980303,
            "供应商交付SLA",
            982005,
            "bar-horizontal",
            x=[chart_field(982005, "vendor_name")],
            y=[chart_field(982005, "sla_hit_rate_pct", summary="avg", formatter=pct), chart_field(982005, "delivered_count")],
            drill=[chart_field(982005, "vendor_name"), chart_field(982005, "vendor_type"), chart_field(982005, "sla_level")],
            color=LIGHT_THEME["teal"],
            scene_id=980003,
        ),
        chart_def(
            980304,
            "预算执行分析",
            982015,
            "bar-group",
            x=[chart_field(982015, "budget_category")],
            y=[chart_field(982015, "approved_amount"), chart_field(982015, "actual_amount")],
            drill=[chart_field(982015, "department"), chart_field(982015, "project_name"), chart_field(982015, "month_label")],
            color=LIGHT_THEME["accent"],
            scene_id=980003,
        ),
        chart_def(
            980305,
            "人员容量明细",
            982011,
            "table-info",
            x=[chart_field(982011, f[0]) for f in DATASET_BY_GROUP[982011]["fields"]],
            y=[],
            drill=[chart_field(982011, "department"), chart_field(982011, "person_name"), chart_field(982011, "vendor_name")],
            color=LIGHT_THEME["teal"],
            scene_id=980003,
        ),
        chart_def(
            980306,
            "供应商经营明细",
            982005,
            "table-info",
            x=[chart_field(982005, f[0]) for f in DATASET_BY_GROUP[982005]["fields"]],
            y=[],
            drill=[chart_field(982005, "vendor_name"), chart_field(982005, "vendor_type")],
            color=LIGHT_THEME["primary"],
            scene_id=980003,
        ),
        chart_def(
            980401,
            "工时/代码行实时趋势",
            982008,
            "line",
            x=[chart_field(982008, "stat_date")],
            y=[chart_field(982008, "total_hours"), chart_field(982008, "net_lines")],
            drill=[chart_field(982008, "department"), chart_field(982008, "project_name"), chart_field(982008, "person_name")],
            color=LIGHT_THEME["primary"],
            scene_id=980004,
        ),
        chart_def(
            980402,
            "工时价值分类",
            982009,
            "bar-horizontal",
            x=[chart_field(982009, "work_type")],
            y=[chart_field(982009, "total_hours"), chart_field(982009, "commit_count"), chart_field(982009, "net_lines")],
            drill=[chart_field(982009, "work_type"), chart_field(982009, "department")],
            color=LIGHT_THEME["accent"],
            scene_id=980004,
        ),
        chart_def(
            980403,
            "DORA交付趋势",
            982013,
            "line",
            x=[chart_field(982013, "month_label")],
            y=[
                chart_field(982013, "deployment_count"),
                chart_field(982013, "avg_change_lead_time_days", summary="avg"),
                chart_field(982013, "change_failure_rate_pct", summary="avg", formatter=pct),
            ],
            stack=[chart_field(982013, "department")],
            drill=[chart_field(982013, "department"), chart_field(982013, "project_name")],
            color=LIGHT_THEME["primary"],
            scene_id=980004,
        ),
        chart_def(
            980404,
            "代码与工时明细",
            982008,
            "table-info",
            x=[chart_field(982008, f[0]) for f in DATASET_BY_GROUP[982008]["fields"]],
            y=[],
            drill=[chart_field(982008, "department"), chart_field(982008, "project_name"), chart_field(982008, "person_name")],
            color=LIGHT_THEME["secondary"],
            scene_id=980004,
        ),
        chart_def(
            980405,
            "工程指标口径",
            982012,
            "table-info",
            x=[chart_field(982012, f[0]) for f in DATASET_BY_GROUP[982012]["fields"]],
            y=[],
            drill=[chart_field(982012, "domain_name"), chart_field(982012, "metric_name")],
            color=LIGHT_THEME["muted"],
            scene_id=980004,
        ),
        chart_def(
            980501,
            "质量风险矩阵",
            982014,
            "bar-group",
            x=[chart_field(982014, "project_name")],
            y=[
                chart_field(982014, "test_defect_density", summary="avg"),
                chart_field(982014, "production_problem_density", summary="avg"),
                chart_field(982014, "p1_p2_incidents"),
            ],
            drill=[chart_field(982014, "department"), chart_field(982014, "project_name"), chart_field(982014, "risk_level")],
            color=LIGHT_THEME["rose"],
            scene_id=980005,
        ),
        chart_def(
            980502,
            "变更失败与恢复",
            982013,
            "line",
            x=[chart_field(982013, "month_label")],
            y=[
                chart_field(982013, "change_failure_rate_pct", summary="avg", formatter=pct),
                chart_field(982013, "avg_recovery_time_hours", summary="avg"),
                chart_field(982013, "rework_deployment_rate_pct", summary="avg", formatter=pct),
            ],
            stack=[chart_field(982013, "department")],
            drill=[chart_field(982013, "department"), chart_field(982013, "project_name")],
            color=LIGHT_THEME["accent"],
            scene_id=980005,
        ),
        chart_def(
            980503,
            "项目风险清单",
            982010,
            "table-info",
            x=[chart_field(982010, f[0]) for f in DATASET_BY_GROUP[982010]["fields"]],
            y=[],
            drill=[chart_field(982010, "department"), chart_field(982010, "project_name"), chart_field(982010, "risk_level")],
            color=LIGHT_THEME["primary"],
            scene_id=980005,
        ),
        chart_def(
            980504,
            "质量风险明细",
            982014,
            "table-info",
            x=[chart_field(982014, f[0]) for f in DATASET_BY_GROUP[982014]["fields"]],
            y=[],
            drill=[chart_field(982014, "department"), chart_field(982014, "project_name"), chart_field(982014, "risk_level")],
            color=LIGHT_THEME["teal"],
            scene_id=980005,
        ),
        chart_def(
            980505,
            "质量指标口径",
            982012,
            "table-info",
            x=[chart_field(982012, f[0]) for f in DATASET_BY_GROUP[982012]["fields"]],
            y=[],
            drill=[chart_field(982012, "domain_name"), chart_field(982012, "metric_name")],
            color=LIGHT_THEME["muted"],
            scene_id=980005,
        ),
    ]
    for flow_chart in [chart for chart in charts if chart["id"] in {980107, 980201}]:
        flow_chart["custom_attr"]["basicStyle"]["colors"] = LIGHT_THEME["flow_palette"]
        flow_chart["custom_attr"]["basicStyle"]["lineSmooth"] = True
        flow_chart["custom_attr"]["basicStyle"]["lineWidth"] = 1
        flow_chart["custom_attr"]["basicStyle"]["lineSymbolSize"] = 0
        flow_chart["custom_attr"]["basicStyle"]["alpha"] = 88
        flow_chart["custom_attr"]["basicStyle"]["gradient"] = True
        flow_chart["custom_attr"]["basicStyle"]["cumulativeFlow"] = True
        flow_chart["custom_attr"]["tooltip"]["trigger"] = "axis"
        flow_chart["custom_style"]["legend"].update(
            {"show": True, "hPosition": "left", "vPosition": "top", "orient": "vertical", "fontSize": 12}
        )
        flow_chart["custom_style"]["misc"]["padding"] = {"top": 40, "right": 16, "bottom": 20, "left": 118}
        flow_chart["senior"] = copy.deepcopy(SENIOR)
        flow_chart["senior"]["functionCfg"]["emptyDataStrategy"] = "ignoreData"

    stage_chart = next(chart for chart in charts if chart["id"] == 980202)
    stage_chart["custom_attr"]["basicStyle"]["colors"] = LIGHT_THEME["flow_palette"]
    stage_chart["custom_attr"]["basicStyle"]["stageFunnel"] = {
        "periodLabel": "5月流入",
        "periodColor": LIGHT_THEME["primary"],
        "metricTitle": "真实阶段事件：流入需求数 / 平均耗时 / P90 / SLE超期",
        "metricLimit": 4,
        "metricAliases": {
            "需求数": "数",
            "平均耗时天": "均",
            "P90耗时天": "P90",
            "SLE超期数": "超",
        },
        "noteTitleColor": LIGHT_THEME["text"],
        "noteTextColor": LIGHT_THEME["muted"],
    }
    stage_chart["custom_attr"]["label"]["show"] = True
    stage_chart["custom_attr"]["label"]["fontSize"] = 15
    stage_chart["custom_style"]["misc"]["padding"] = {"top": 20, "right": 20, "bottom": 20, "left": 20}

    matrix_chart = next(chart for chart in charts if chart["id"] == 980301)
    matrix_chart["custom_attr"]["basicStyle"]["colors"] = [
        LIGHT_THEME["rose"],
        LIGHT_THEME["accent"],
        LIGHT_THEME["green"],
    ]
    matrix_chart["custom_attr"]["basicStyle"]["scatterSymbolSize"] = 10
    matrix_chart["custom_attr"]["basicStyle"]["metricMatrix"] = {
        "xReference": 82,
        "yReference": 4.5,
        "xReferenceLabel": "投入基线 82人天",
        "yReferenceLabel": "产出基线 4.5个需求",
        "referenceLineColor": LIGHT_THEME["area_border"],
        "referenceTextColor": LIGHT_THEME["muted"],
        "quadrants": [
            {"label": "高产低投", "position": "top-left", "color": LIGHT_THEME["soft_teal"]},
            {"label": "高产高投", "position": "top-right", "color": LIGHT_THEME["soft_blue"]},
            {"label": "低产低投", "position": "bottom-left", "color": "rgba(245,166,35,.10)"},
            {"label": "低产高投", "position": "bottom-right", "color": "rgba(239,90,122,.10)"},
        ],
        "colorMapping": {
            "低效率": LIGHT_THEME["rose"],
            "中效率": LIGHT_THEME["accent"],
            "高效率": LIGHT_THEME["green"],
        },
        "pointSizeRange": [5, 12],
        "pointStroke": LIGHT_THEME["surface"],
        "pointShadowBlur": 6,
        "pointShadowColor": "rgba(59,130,246,.14)",
        "referenceLines": [
            {"label": "高效率 >=0.09", "value": 0.09, "color": LIGHT_THEME["green"]},
            {"label": "中效率 0.05", "value": 0.05, "color": LIGHT_THEME["accent"]},
        ]
    }
    matrix_chart["custom_attr"]["label"]["show"] = True
    matrix_chart["custom_attr"]["label"]["fontSize"] = 10
    matrix_chart["custom_attr"]["tooltip"]["show"] = True
    matrix_chart["custom_style"]["misc"]["padding"] = {"top": 52, "right": 34, "bottom": 36, "left": 62}
    return charts


CHARTS = build_charts()


def events():
    return {
        "jump": {"type": "_blank", "value": "https://"},
        "type": "jump",
        "share": {"value": True},
        "checked": False,
        "download": {"value": True},
        "showTips": False,
        "typeList": [
            {"key": "jump", "label": "jump"},
            {"key": "download", "label": "download"},
            {"key": "share", "label": "share"},
            {"key": "fullScreen", "label": "fullScreen"},
            {"key": "showHidden", "label": "showHidden"},
            {"key": "refreshDataV", "label": "refreshDataV"},
            {"key": "refreshView", "label": "refreshView"},
        ],
        "showHidden": {"value": True},
        "refreshView": {"value": True, "target": "all"},
        "refreshDataV": {"value": True},
    }


def component_background(color):
    return {
        "innerImage": "board/board_1.svg",
        "outerImage": None,
        "borderRadius": {"mode": "uniform", "topLeft": 8, "topRight": 8, "bottomLeft": 8, "bottomRight": 8},
        "innerPadding": {"top": 12, "left": 12, "mode": "uniform", "right": 12, "bottom": 12},
        "backdropFilter": 0,
        "backgroundType": "backgroundColor",
        "backgroundColor": LIGHT_THEME["surface_alpha"],
        "innerImageColor": color,
        "backdropFilterEnable": False,
        "backgroundColorSelect": True,
        "backgroundImageEnable": False,
    }


def chart_component(chart_id, title, inner_type, left, top, width, height, color):
    return {
        "x": 1,
        "y": 1,
        "id": str(chart_id),
        "icon": inner_type,
        "name": title,
        "label": title,
        "sizeX": max(1, round(width / 12)),
        "sizeY": max(1, round(height / 12)),
        "state": "ready",
        "style": {
            "top": top,
            "left": left,
            "width": width,
            "height": height,
            "rotate": 0,
            "opacity": 1,
            "adaptation": "adaptation",
            "borderColor": LIGHT_THEME["border"],
            "borderStyle": "solid",
            "borderWidth": 1,
            "borderActive": False,
            "borderRadius": 6,
        },
        "events": events(),
        "isLock": False,
        "isShow": True,
        "render": "custom" if inner_type == "indicator" else "antv",
        "editing": False,
        "linkage": {"data": [], "duration": 0},
        "canvasId": "canvas-main",
        "carousel": {"time": 10, "enable": False},
        "category": "base",
        "dragging": False,
        "resizing": False,
        "component": "UserView",
        "innerType": inner_type,
        "propValue": {"urlList": [], "textValue": ""},
        "animations": [],
        "groupStyle": {},
        "aspectRatio": 1,
        "matrixStyle": {},
        "canvasActive": False,
        "collapseName": [
            "position",
            "background",
            "style",
            "picture",
            "frameLinks",
            "videoLinks",
            "streamLinks",
            "carouselInfo",
            "events",
            "decoration_style",
        ],
        "maintainRadio": False,
        "actionSelection": {"linkageActive": "auto"},
        "dashboardHidden": False,
        "commonBackground": component_background(color),
        "multiDimensional": {"x": 0, "y": 0, "z": 0, "enable": False},
    }


def title_component(screen):
    title_html = (
        f'<div style="font-size:36px;font-weight:850;line-height:42px;letter-spacing:0;'
        f'text-align:center;color:{LIGHT_THEME["text"]};text-shadow:{LIGHT_THEME["title_shadow"]}">'
        f'{screen["name"]}</div>'
        f'<div style="margin-top:6px;font-size:15px;font-weight:500;'
        f'color:{LIGHT_THEME["muted"]};text-align:center">{screen["subtitle"]}</div>'
    )
    return {
        "x": 1,
        "y": 1,
        "id": f"{screen['id']}-title",
        "icon": "icon_text",
        "name": "大屏标题",
        "label": "大屏标题",
        "sizeX": 154,
        "sizeY": 6,
        "state": "ready",
        "style": {
            "top": 10,
            "left": 36,
            "color": LIGHT_THEME["text"],
            "width": 1848,
            "height": 68,
            "rotate": 0,
            "opacity": 1,
            "padding": 0,
            "fontSize": 36,
            "textAlign": "center",
            "adaptation": "adaptation",
            "fontWeight": 700,
            "borderColor": "rgba(0,0,0,0)",
            "borderStyle": "solid",
            "borderWidth": 0,
            "borderActive": False,
            "borderRadius": 0,
            "letterSpacing": 0,
            "verticalAlign": "middle",
        },
        "events": events(),
        "isLock": False,
        "isShow": True,
        "editing": False,
        "linkage": {"data": [], "duration": 0},
        "canvasId": "canvas-main",
        "carousel": {"time": 10, "enable": False},
        "category": "base",
        "dragging": False,
        "resizing": False,
        "component": "VText",
        "innerType": "VText",
        "propValue": title_html,
        "animations": [],
        "groupStyle": {},
        "aspectRatio": 1,
        "matrixStyle": {},
        "canvasActive": False,
        "collapseName": ["position", "background", "style"],
        "maintainRadio": False,
        "dashboardHidden": False,
        "commonBackground": {
            "innerImage": "board/board_1.svg",
            "outerImage": None,
            "borderRadius": {"mode": "uniform", "topLeft": 0, "topRight": 0, "bottomLeft": 0, "bottomRight": 0},
            "innerPadding": {"top": 0, "left": 0, "mode": "uniform", "right": 0, "bottom": 0},
            "backdropFilter": 0,
            "backgroundType": "innerImage",
            "backgroundColor": "rgba(0,0,0,0)",
            "innerImageColor": "rgba(0,0,0,0)",
            "backdropFilterEnable": False,
            "backgroundColorSelect": False,
            "backgroundImageEnable": False,
        },
        "multiDimensional": {"x": 0, "y": 0, "z": 0, "enable": False},
    }


def canvas_style():
    return {
        "width": 1920,
        "height": 1080,
        "scale": 100,
        "scaleWidth": 100,
        "scaleHeight": 100,
        "screenAdaptor": "widthFirst",
        "dashboardAdaptor": "keepHeightAndWidth",
        "backgroundColorSelect": True,
        "backgroundImageEnable": False,
        "backgroundType": "backgroundColor",
        "background": "",
        "opacity": 1,
        "fontSize": 14,
        "fontFamily": "PingFang SC",
        "themeId": "10002",
        "color": LIGHT_THEME["text"],
        "backgroundColor": LIGHT_THEME["bg"],
        "refreshViewEnable": True,
        "refreshViewLoading": True,
        "refreshUnit": "minute",
        "refreshTime": 5,
        "openCommonStyle": True,
        "popupAvailable": True,
        "popupButtonAvailable": True,
        "suspensionButtonAvailable": False,
        "dialogBackgroundColor": LIGHT_THEME["surface"],
        "dialogButton": LIGHT_THEME["primary"],
        "dashboard": {
            "gap": "yes",
            "gapSize": 5,
            "resultMode": "all",
            "resultCount": 1000,
            "themeColor": "light",
            "showGrid": False,
            "matrixBase": 4,
            "gapMode": "middle",
            "mobileSetting": {"customSetting": False, "imageUrl": None, "backgroundType": "image", "color": LIGHT_THEME["bg"]},
        },
        "component": {
            "chartTitle": {
                "show": True,
                "fontSize": "18",
                "hPosition": "left",
                "vPosition": "top",
                "isItalic": False,
                "isBolder": True,
                "remarkShow": False,
                "remark": "",
                "fontFamily": "PingFang SC",
                "letterSpace": "0",
                "fontShadow": False,
                "color": LIGHT_THEME["text"],
                "remarkBackgroundColor": LIGHT_THEME["surface"],
            },
            "chartColor": common_attr("默认"),
            "chartCommonStyle": {
                "backgroundColorSelect": True,
                "backgroundImageEnable": False,
                "backgroundType": "backgroundColor",
                "innerImage": "board/board_1.svg",
                "outerImage": None,
                "innerPadding": 12,
                "borderRadius": 8,
                "backgroundColor": LIGHT_THEME["surface_alpha"],
                "innerImageColor": LIGHT_THEME["primary"],
            },
            "filterStyle": {
                "layout": "horizontal",
                "titleLayout": "left",
                "labelColor": LIGHT_THEME["text"],
                "titleColor": LIGHT_THEME["text"],
                "color": LIGHT_THEME["text"],
                "borderColor": LIGHT_THEME["border"],
                "text": LIGHT_THEME["muted"],
                "bgColor": LIGHT_THEME["surface"],
            },
            "tabStyle": {
                "headPosition": "left",
                "headFontColor": LIGHT_THEME["muted"],
                "headFontActiveColor": LIGHT_THEME["primary"],
                "headBorderColor": LIGHT_THEME["border"],
                "headBorderActiveColor": LIGHT_THEME["primary"],
            },
            "seniorStyleSetting": {
                "linkageIconColor": LIGHT_THEME["muted"],
                "drillLayerColor": LIGHT_THEME["muted"],
                "pagerColor": LIGHT_THEME["muted"],
                "pagerSize": 14,
            },
            "formatterItem": {},
        },
    }


SCREEN_CONFIGS = [
    {
        "id": 980001,
        "name": "研发经营总览大屏",
        "subtitle": "吞吐量 / 人力容量 / DORA交付 / 预算执行 / 风险下钻",
        "content_id": "eng-efficiency-overview",
        "remark": "面向总经理室和科技管理层的全局经营视图",
        "layout": {
            980101: (36, 92, 294, 104),
            980102: (346, 92, 294, 104),
            980103: (656, 92, 294, 104),
            980104: (966, 92, 294, 104),
            980105: (1276, 92, 294, 104),
            980106: (1586, 92, 294, 104),
            980107: (36, 216, 560, 300),
            980108: (614, 216, 430, 300),
            980109: (1062, 216, 390, 300),
            980117: (1470, 216, 414, 300),
            980118: (36, 534, 520, 230),
            980110: (574, 534, 470, 230),
            980115: (1062, 534, 822, 230),
            980119: (36, 782, 1848, 264),
        },
    },
    {
        "id": 980002,
        "name": "需求流动与交付效率大屏",
        "subtitle": "累积流 / 漏斗WIP / 分段时效 / 吞吐趋势 / 需求明细",
        "content_id": "eng-flow-delivery",
        "remark": "面向需求经理、项目经理和研发管理的流动效率视图",
        "layout": {
            980201: (36, 92, 1160, 430),
            980202: (1214, 92, 670, 430),
            980204: (36, 540, 560, 230),
            980203: (614, 540, 560, 230),
            980206: (1192, 540, 692, 230),
            980205: (36, 788, 1848, 258),
        },
    },
    {
        "id": 980003,
        "name": "人力容量与供应商经营大屏",
        "subtitle": "角色分布 / 容量需求比 / 外包供应商 / 预算成本 / 人员明细",
        "content_id": "eng-capacity-vendor",
        "remark": "面向科技人力、供应商和部门负责人的资源经营视图",
        "layout": {
            980301: (36, 92, 720, 300),
            980302: (774, 92, 480, 300),
            980303: (1272, 92, 612, 300),
            980304: (36, 410, 720, 260),
            980306: (774, 410, 1110, 260),
            980305: (36, 688, 1848, 358),
        },
    },
    {
        "id": 980004,
        "name": "工程活动与代码效能大屏",
        "subtitle": "工时价值分类 / 代码变更规模 / DORA交付趋势 / 明细追踪",
        "content_id": "eng-engineering-activity",
        "remark": "面向工程效能、架构和DevOps团队的工程活动视图",
        "layout": {
            980401: (36, 92, 900, 300),
            980402: (954, 92, 930, 300),
            980403: (36, 410, 900, 260),
            980405: (954, 410, 930, 260),
            980404: (36, 688, 1848, 358),
        },
    },
    {
        "id": 980005,
        "name": "质量稳定性与风险预警大屏",
        "subtitle": "缺陷密度 / 生产问题 / 变更失败 / 恢复时间 / 风险清单",
        "content_id": "eng-quality-risk",
        "remark": "面向质量、运维和系统负责人的稳定性风险视图",
        "layout": {
            980501: (36, 92, 900, 300),
            980502: (954, 92, 930, 300),
            980504: (36, 410, 900, 260),
            980505: (954, 410, 930, 260),
            980503: (36, 688, 1848, 358),
        },
    },
]


CHART_COLORS = {
    980101: LIGHT_THEME["primary"],
    980102: LIGHT_THEME["teal"],
    980103: LIGHT_THEME["accent"],
    980104: LIGHT_THEME["rose"],
    980105: LIGHT_THEME["secondary"],
    980106: LIGHT_THEME["teal"],
    980107: LIGHT_THEME["primary"],
    980108: LIGHT_THEME["teal"],
    980109: LIGHT_THEME["accent"],
    980110: LIGHT_THEME["rose"],
    980111: LIGHT_THEME["secondary"],
    980112: LIGHT_THEME["teal"],
    980113: LIGHT_THEME["primary"],
    980114: LIGHT_THEME["accent"],
    980115: LIGHT_THEME["primary"],
    980116: LIGHT_THEME["teal"],
}


def component_data(screen):
    chart_by_id = {chart["id"]: chart for chart in CHARTS}
    components = [title_component(screen)]
    for chart_id in sorted(screen["layout"]):
        left, top, width, height = screen["layout"][chart_id]
        chart = chart_by_id[chart_id]
        color = CHART_COLORS.get(chart_id, chart["custom_attr"]["basicStyle"]["colors"][0])
        components.append(
            chart_component(chart_id, chart["title"], chart["type"], left, top, width, height, color)
        )
    return components


def dataset_sql():
    rows_group = []
    rows_table = []
    rows_field = []
    for dataset in DATASETS:
        info_inner = {"table": dataset["view"], "remark": "研发效能演示数据集", "datasourceId": DATASOURCE_ID}
        ds = {
            "currentDs": {
                "id": dataset["table_id"],
                "name": dataset["name"],
                "tableName": dataset["view"],
                "datasourceId": DATASOURCE_ID,
                "datasetGroupId": dataset["group_id"],
                "type": "db",
                "info": json_text(info_inner),
                "sqlVariableDetails": None,
                "fields": None,
                "lastUpdateTime": NOW_MS,
                "status": None,
                "isCross": False,
            },
            "currentDsField": [field["id"] for field in dataset["field_objs"]],
            "currentDsFields": dataset["field_objs"],
            "childrenDs": [],
            "unionToParent": {"unionType": None, "unionFields": []},
            "allChildCount": 0,
        }
        rows_group.append(
            (
                dataset["group_id"],
                dataset["name"],
                0,
                0,
                "dataset",
                "db",
                0,
                json_text([ds]),
                "1",
                NOW_MS,
                None,
                "Success",
                "1",
                NOW_MS,
                None,
                0,
            )
        )
        rows_table.append(
            (
                dataset["table_id"],
                dataset["name"],
                dataset["view"],
                DATASOURCE_ID,
                dataset["group_id"],
                "db",
                json_text(info_inner),
                None,
            )
        )
        for field in dataset["field_objs"]:
            rows_field.append(
                (
                    field["id"],
                    field["datasourceId"],
                    field["datasetTableId"],
                    field["datasetGroupId"],
                    None,
                    field["originName"],
                    field["name"],
                    field["description"],
                    field["dataeaseName"],
                    field["fieldShortName"],
                    json_text(field["groupList"]),
                    None,
                    field["groupType"],
                    field["type"],
                    field["size"],
                    field["deType"],
                    field["deExtractType"],
                    field["extField"],
                    1,
                    field["columnIndex"],
                    field["lastSyncTime"],
                    field["accuracy"],
                    None,
                    None,
                    json_text([]),
                    0,
                )
            )
    sql = []
    sql += insert_many(
        "crest.core_dataset_group",
        [
            "id",
            "name",
            "pid",
            "level",
            "node_type",
            "type",
            "mode",
            "info",
            "create_by",
            "create_time",
            "qrtz_instance",
            "sync_status",
            "update_by",
            "last_update_time",
            "union_sql",
            "is_cross",
        ],
        rows_group,
        100,
    )
    sql += insert_many(
        "crest.core_dataset_table",
        ["id", "name", "table_name", "datasource_id", "dataset_group_id", "type", "info", "sql_variable_details"],
        rows_table,
        100,
    )
    sql += insert_many(
        "crest.core_dataset_table_field",
        [
            "id",
            "datasource_id",
            "dataset_table_id",
            "dataset_group_id",
            "chart_id",
            "origin_name",
            "name",
            "description",
            "dataease_name",
            "field_short_name",
            "group_list",
            "other_group",
            "group_type",
            "type",
            "size",
            "de_type",
            "de_extract_type",
            "ext_field",
            "checked",
            "column_index",
            "last_sync_time",
            "accuracy",
            "date_format",
            "date_format_type",
            "params",
            "order_checked",
        ],
        rows_field,
        500,
    )
    return sql


def chart_sql():
    rows = []
    for chart in CHARTS:
        rows.append(
            (
                chart["id"],
                chart["title"],
                chart["scene_id"],
                chart["group_id"],
                chart["type"],
                chart["render"],
                1000,
                "all",
                json_text(chart["x_axis"]),
                json_text(chart["x_axis_ext"]),
                json_text(chart["y_axis"]),
                json_text(chart["y_axis_ext"]),
                json_text(chart["ext_stack"]),
                json_text(chart["ext_bubble"]),
                json_text(chart["ext_label"]),
                json_text(chart["ext_tooltip"]),
                json_text(chart["custom_attr"]),
                None,
                json_text(chart["custom_style"]),
                None,
                json_text({"logic": None, "items": None}),
                json_text(chart["drill_fields"]),
                json_text(chart.get("senior", SENIOR)),
                "1",
                NOW_MS,
                NOW_MS,
                None,
                "panel",
                "private",
                None,
                "dataset",
                json_text(chart["view_fields"]),
                1,
                "minute",
                5,
                0,
                0,
                None,
                None,
                None,
                None,
                None,
                json_text(chart["ext_color"]),
                None,
            )
        )
    return insert_many(
        "crest.core_chart_view",
        [
            "id",
            "title",
            "scene_id",
            "table_id",
            "type",
            "render",
            "result_count",
            "result_mode",
            "x_axis",
            "x_axis_ext",
            "y_axis",
            "y_axis_ext",
            "ext_stack",
            "ext_bubble",
            "ext_label",
            "ext_tooltip",
            "custom_attr",
            "custom_attr_mobile",
            "custom_style",
            "custom_style_mobile",
            "custom_filter",
            "drill_fields",
            "senior",
            "create_by",
            "create_time",
            "update_time",
            "snapshot",
            "style_priority",
            "chart_type",
            "is_plugin",
            "data_from",
            "view_fields",
            "refresh_view_enable",
            "refresh_unit",
            "refresh_time",
            "linkage_active",
            "jump_active",
            "copy_from",
            "copy_id",
            "aggregate",
            "flow_map_start_name",
            "flow_map_end_name",
            "ext_color",
            "sort_priority",
        ],
        rows,
        100,
    )


def screen_sql():
    rows = []
    for index, screen in enumerate(SCREEN_CONFIGS, start=1):
        rows.append(
            (
                str(screen["id"]),
                screen["name"],
                "0",
                "1",
                0,
                "leaf",
                "dataV",
                json_text(canvas_style()),
                json_text(component_data(screen)),
                0,
                1,
                0,
                index,
                NOW_MS,
                "1",
                NOW_MS,
                "1",
                screen["remark"],
                "local-demo",
                0,
                None,
                None,
                3,
                screen["content_id"],
                "1",
            )
        )
    return insert_many(
        "crest.data_visualization_info",
        [
            "id",
            "name",
            "pid",
            "org_id",
            "level",
            "node_type",
            "type",
            "canvas_style_data",
            "component_data",
            "mobile_layout",
            "status",
            "self_watermark_status",
            "sort",
            "create_time",
            "create_by",
            "update_time",
            "update_by",
            "remark",
            "source",
            "delete_flag",
            "delete_time",
            "delete_by",
            "version",
            "content_id",
            "check_version",
        ],
        rows,
    )


DDL = """
SET NAMES utf8mb4;
CREATE DATABASE IF NOT EXISTS crest_demo_retail DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE crest_demo_retail;

DROP VIEW IF EXISTS v_eng_person_drill;
DROP VIEW IF EXISTS v_eng_project_drill;
DROP VIEW IF EXISTS v_eng_efficiency_matrix;
DROP VIEW IF EXISTS v_eng_stage_funnel;
DROP VIEW IF EXISTS v_eng_demand_funnel;
DROP VIEW IF EXISTS v_eng_budget_execution;
DROP VIEW IF EXISTS v_eng_quality_risk;
DROP VIEW IF EXISTS v_eng_dora_metrics;
DROP VIEW IF EXISTS v_eng_metric_catalog;
DROP VIEW IF EXISTS v_eng_work_type_summary;
DROP VIEW IF EXISTS v_eng_work_code_daily;
DROP VIEW IF EXISTS v_eng_requirement_active_flow;
DROP VIEW IF EXISTS v_eng_requirement_flow;
DROP VIEW IF EXISTS v_eng_stage_aging;
DROP VIEW IF EXISTS v_eng_vendor_management;
DROP VIEW IF EXISTS v_eng_role_distribution;
DROP VIEW IF EXISTS v_eng_capacity_demand;
DROP VIEW IF EXISTS v_eng_throughput_monthly;
DROP VIEW IF EXISTS v_eng_kpi;

DROP TABLE IF EXISTS eng_metric_definition;
DROP TABLE IF EXISTS eng_fact_budget_monthly;
DROP TABLE IF EXISTS eng_fact_incident;
DROP TABLE IF EXISTS eng_fact_defect;
DROP TABLE IF EXISTS eng_fact_deployment;
DROP TABLE IF EXISTS eng_fact_requirement_snapshot_daily;
DROP TABLE IF EXISTS eng_fact_requirement_stage_event;
DROP TABLE IF EXISTS eng_fact_capacity_monthly;
DROP TABLE IF EXISTS eng_fact_code_daily;
DROP TABLE IF EXISTS eng_fact_worklog_daily;
DROP TABLE IF EXISTS eng_fact_requirement;
DROP TABLE IF EXISTS eng_dim_project;
DROP TABLE IF EXISTS eng_dim_employee;
DROP TABLE IF EXISTS eng_dim_vendor;
DROP TABLE IF EXISTS eng_dim_team;

CREATE TABLE eng_dim_team (
  team_id BIGINT PRIMARY KEY,
  department VARCHAR(64) NOT NULL,
  product_line VARCHAR(64) NOT NULL,
  tribe VARCHAR(64) NOT NULL,
  manager_name VARCHAR(64) NOT NULL,
  cost_center VARCHAR(64) NOT NULL
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE eng_dim_vendor (
  vendor_id BIGINT PRIMARY KEY,
  vendor_name VARCHAR(64) NOT NULL,
  vendor_type VARCHAR(64) NOT NULL,
  region VARCHAR(64) NOT NULL,
  contract_mode VARCHAR(64) NOT NULL,
  sla_level VARCHAR(16) NOT NULL,
  hourly_rate DECIMAL(10,2) NOT NULL,
  active_status VARCHAR(32) NOT NULL
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE eng_dim_employee (
  employee_id BIGINT PRIMARY KEY,
  employee_name VARCHAR(64) NOT NULL,
  team_id BIGINT NOT NULL,
  vendor_id BIGINT NULL,
  employment_type VARCHAR(32) NOT NULL,
  role_name VARCHAR(64) NOT NULL,
  seniority VARCHAR(32) NOT NULL,
  cost_rate DECIMAL(10,2) NOT NULL,
  start_date DATE NOT NULL,
  capacity_hours_month DECIMAL(10,1) NOT NULL,
  active_status VARCHAR(32) NOT NULL
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE eng_dim_project (
  project_id BIGINT PRIMARY KEY,
  project_code VARCHAR(32) NOT NULL,
  project_name VARCHAR(128) NOT NULL,
  team_id BIGINT NOT NULL,
  business_domain VARCHAR(64) NOT NULL,
  priority VARCHAR(16) NOT NULL,
  product_owner VARCHAR(64) NOT NULL
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE eng_fact_requirement (
  requirement_id BIGINT PRIMARY KEY,
  requirement_code VARCHAR(64) NOT NULL,
  project_id BIGINT NOT NULL,
  team_id BIGINT NOT NULL,
  vendor_id BIGINT NULL,
  requirement_type VARCHAR(32) NOT NULL,
  priority VARCHAR(16) NOT NULL,
  status VARCHAR(32) NOT NULL,
  create_date DATE NOT NULL,
  planned_online_date DATE NOT NULL,
  online_date DATE NULL,
  lead_time_days INT NOT NULL,
  discovery_days INT NOT NULL,
  design_days INT NOT NULL,
  dev_days INT NOT NULL,
  test_days INT NOT NULL,
  waiting_days INT NOT NULL,
  rework_count INT NOT NULL,
  story_points INT NOT NULL,
  demand_hours DECIMAL(10,1) NOT NULL,
  delivered_flag TINYINT NOT NULL,
  INDEX idx_req_project (project_id),
  INDEX idx_req_team_date (team_id, create_date),
  INDEX idx_req_status (status)
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE eng_fact_worklog_daily (
  worklog_id BIGINT PRIMARY KEY,
  work_date DATE NOT NULL,
  employee_id BIGINT NOT NULL,
  project_id BIGINT NOT NULL,
  requirement_id BIGINT NULL,
  work_type VARCHAR(64) NOT NULL,
  hours DECIMAL(10,1) NOT NULL,
  billable_hours DECIMAL(10,1) NOT NULL,
  INDEX idx_work_date (work_date),
  INDEX idx_work_employee (employee_id),
  INDEX idx_work_project (project_id)
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE eng_fact_code_daily (
  code_id BIGINT PRIMARY KEY,
  code_date DATE NOT NULL,
  employee_id BIGINT NOT NULL,
  project_id BIGINT NOT NULL,
  requirement_id BIGINT NULL,
  repo_name VARCHAR(128) NOT NULL,
  language VARCHAR(64) NOT NULL,
  category VARCHAR(64) NOT NULL,
  commit_count INT NOT NULL,
  lines_added INT NOT NULL,
  lines_deleted INT NOT NULL,
  files_changed INT NOT NULL,
  INDEX idx_code_date (code_date),
  INDEX idx_code_employee (employee_id),
  INDEX idx_code_project (project_id)
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE eng_fact_capacity_monthly (
  capacity_id BIGINT PRIMARY KEY,
  month_label VARCHAR(16) NOT NULL,
  team_id BIGINT NOT NULL,
  role_name VARCHAR(64) NOT NULL,
  headcount INT NOT NULL,
  available_hours DECIMAL(10,1) NOT NULL,
  demand_hours DECIMAL(10,1) NOT NULL,
  assigned_hours DECIMAL(10,1) NOT NULL,
  gap_hours DECIMAL(10,1) NOT NULL,
  INDEX idx_capacity_month_team (month_label, team_id)
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE eng_fact_requirement_stage_event (
  event_id BIGINT PRIMARY KEY,
  requirement_id BIGINT NOT NULL,
  stage_name VARCHAR(32) NOT NULL,
  stage_order INT NOT NULL,
  entered_date DATE NOT NULL,
  left_date DATE NULL,
  duration_days INT NOT NULL,
  INDEX idx_stage_req (requirement_id),
  INDEX idx_stage_name (stage_name),
  INDEX idx_stage_entered (entered_date)
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE eng_fact_requirement_snapshot_daily (
  snapshot_id BIGINT PRIMARY KEY,
  snapshot_date DATE NOT NULL,
  requirement_id BIGINT NOT NULL,
  stage_name VARCHAR(32) NOT NULL,
  stage_order INT NOT NULL,
  stage_entered_date DATE NOT NULL,
  age_days INT NOT NULL,
  completed_flag TINYINT NOT NULL,
  INDEX idx_snapshot_date_stage (snapshot_date, stage_name),
  INDEX idx_snapshot_req (requirement_id)
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE eng_fact_deployment (
  deployment_id BIGINT PRIMARY KEY,
  requirement_id BIGINT NOT NULL,
  project_id BIGINT NOT NULL,
  team_id BIGINT NOT NULL,
  deploy_date DATE NOT NULL,
  environment VARCHAR(32) NOT NULL,
  deployment_status VARCHAR(32) NOT NULL,
  change_lead_time_hours DECIMAL(10,1) NOT NULL,
  failed_flag TINYINT NOT NULL,
  recovery_time_hours DECIMAL(10,1) NOT NULL,
  rework_deploy_flag TINYINT NOT NULL,
  emergency_flag TINYINT NOT NULL,
  batch_size INT NOT NULL,
  INDEX idx_deploy_date (deploy_date),
  INDEX idx_deploy_project (project_id)
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE eng_fact_defect (
  defect_id BIGINT PRIMARY KEY,
  requirement_id BIGINT NOT NULL,
  project_id BIGINT NOT NULL,
  team_id BIGINT NOT NULL,
  vendor_id BIGINT NULL,
  defect_phase VARCHAR(32) NOT NULL,
  severity VARCHAR(16) NOT NULL,
  found_date DATE NOT NULL,
  resolved_date DATE NOT NULL,
  escaped_flag TINYINT NOT NULL,
  INDEX idx_defect_project (project_id),
  INDEX idx_defect_phase (defect_phase),
  INDEX idx_defect_date (found_date)
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE eng_fact_incident (
  incident_id BIGINT PRIMARY KEY,
  defect_id BIGINT NOT NULL,
  deployment_id BIGINT NULL,
  project_id BIGINT NOT NULL,
  team_id BIGINT NOT NULL,
  opened_date DATE NOT NULL,
  severity VARCHAR(16) NOT NULL,
  recovery_time_hours DECIMAL(10,1) NOT NULL,
  root_cause VARCHAR(64) NOT NULL,
  INDEX idx_incident_project (project_id),
  INDEX idx_incident_date (opened_date)
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE eng_fact_budget_monthly (
  budget_id BIGINT PRIMARY KEY,
  month_label VARCHAR(16) NOT NULL,
  project_id BIGINT NOT NULL,
  team_id BIGINT NOT NULL,
  budget_category VARCHAR(32) NOT NULL,
  approved_amount DECIMAL(14,1) NOT NULL,
  actual_amount DECIMAL(14,1) NOT NULL,
  INDEX idx_budget_month_team (month_label, team_id)
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE eng_metric_definition (
  metric_id BIGINT PRIMARY KEY,
  metric_code VARCHAR(64) NOT NULL,
  domain_name VARCHAR(64) NOT NULL,
  metric_name VARCHAR(64) NOT NULL,
  source_system VARCHAR(128) NOT NULL,
  formula_text VARCHAR(512) NOT NULL,
  grain VARCHAR(64) NOT NULL,
  standard_basis VARCHAR(128) NOT NULL,
  refresh_frequency VARCHAR(64) NOT NULL,
  caveat VARCHAR(256) NOT NULL
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
"""


VIEWS = f"""
USE crest_demo_retail;

CREATE OR REPLACE VIEW v_eng_kpi AS
SELECT
  (SELECT COUNT(*) FROM eng_fact_requirement WHERE delivered_flag = 1 AND online_date >= '2026-05-01') AS delivered_requirements_mtd,
  (SELECT COUNT(*) FROM eng_fact_requirement WHERE status NOT IN ('已上线','暂缓')) AS active_requirements,
  (SELECT ROUND(SUM(available_hours) / NULLIF(SUM(demand_hours), 0) * 100, 1) FROM eng_fact_capacity_monthly WHERE month_label = '2026-05') AS capacity_demand_ratio_pct,
  (SELECT ROUND(SUM(hours), 1) FROM eng_fact_worklog_daily WHERE work_date >= '2026-05-01') AS work_hours_mtd,
  (SELECT COALESCE(SUM(lines_added - lines_deleted), 0) FROM eng_fact_code_daily WHERE code_date >= '2026-05-01') AS net_lines_mtd,
  (SELECT ROUND(SUM(CASE WHEN r.online_date <= r.planned_online_date THEN 1 ELSE 0 END) / NULLIF(COUNT(*), 0) * 100, 1)
     FROM eng_fact_requirement r WHERE r.delivered_flag = 1 AND r.vendor_id IS NOT NULL) AS vendor_sla_rate_pct;

CREATE OR REPLACE VIEW v_eng_metric_catalog AS
SELECT domain_name, metric_name, source_system, formula_text, grain, standard_basis, refresh_frequency, caveat
FROM eng_metric_definition;

CREATE OR REPLACE VIEW v_eng_dora_metrics AS
SELECT
  DATE_FORMAT(d.deploy_date, '%Y-%m') AS month_label,
  t.department,
  p.project_name,
  COUNT(*) AS deployment_count,
  ROUND(AVG(d.change_lead_time_hours) / 24, 1) AS avg_change_lead_time_days,
  ROUND(SUM(d.failed_flag) / NULLIF(COUNT(*), 0) * 100, 1) AS change_failure_rate_pct,
  ROUND(AVG(CASE WHEN d.failed_flag = 1 THEN d.recovery_time_hours END), 1) AS avg_recovery_time_hours,
  ROUND(SUM(d.rework_deploy_flag) / NULLIF(COUNT(*), 0) * 100, 1) AS rework_deployment_rate_pct
FROM eng_fact_deployment d
JOIN eng_dim_project p ON p.project_id = d.project_id
JOIN eng_dim_team t ON t.team_id = d.team_id
GROUP BY DATE_FORMAT(d.deploy_date, '%Y-%m'), t.department, p.project_name;

CREATE OR REPLACE VIEW v_eng_quality_risk AS
WITH req AS (
  SELECT project_id, COUNT(*) AS delivered_reqs
  FROM eng_fact_requirement
  WHERE delivered_flag = 1
  GROUP BY project_id
), defect AS (
  SELECT project_id,
         SUM(CASE WHEN defect_phase = '系统测试' THEN 1 ELSE 0 END) AS test_defects,
         SUM(CASE WHEN escaped_flag = 1 THEN 1 ELSE 0 END) AS escaped_defects
  FROM eng_fact_defect
  GROUP BY project_id
), incident AS (
  SELECT project_id,
         SUM(CASE WHEN severity IN ('P1','P2') THEN 1 ELSE 0 END) AS p1_p2_incidents,
         ROUND(AVG(recovery_time_hours), 1) AS avg_recovery_time_hours
  FROM eng_fact_incident
  GROUP BY project_id
)
SELECT
  t.department,
  p.project_name,
  COALESCE(req.delivered_reqs, 0) AS delivered_reqs,
  ROUND(COALESCE(defect.test_defects, 0) / NULLIF(COALESCE(req.delivered_reqs, 0), 0), 2) AS test_defect_density,
  ROUND(COALESCE(defect.escaped_defects, 0) / NULLIF(COALESCE(req.delivered_reqs, 0), 0), 2) AS production_problem_density,
  COALESCE(defect.escaped_defects, 0) AS escaped_defects,
  COALESCE(incident.p1_p2_incidents, 0) AS p1_p2_incidents,
  COALESCE(incident.avg_recovery_time_hours, 0) AS avg_recovery_time_hours,
  CASE
    WHEN COALESCE(incident.p1_p2_incidents, 0) >= 2 OR ROUND(COALESCE(defect.escaped_defects, 0) / NULLIF(COALESCE(req.delivered_reqs, 0), 0), 2) >= 0.35 THEN '高'
    WHEN COALESCE(incident.p1_p2_incidents, 0) >= 1 OR ROUND(COALESCE(defect.escaped_defects, 0) / NULLIF(COALESCE(req.delivered_reqs, 0), 0), 2) >= 0.18 THEN '中'
    ELSE '低'
  END AS risk_level
FROM eng_dim_project p
JOIN eng_dim_team t ON t.team_id = p.team_id
LEFT JOIN req ON req.project_id = p.project_id
LEFT JOIN defect ON defect.project_id = p.project_id
LEFT JOIN incident ON incident.project_id = p.project_id;

CREATE OR REPLACE VIEW v_eng_budget_execution AS
SELECT
  b.month_label,
  t.department,
  p.project_name,
  b.budget_category,
  ROUND(SUM(b.approved_amount), 1) AS approved_amount,
  ROUND(SUM(b.actual_amount), 1) AS actual_amount,
  ROUND(SUM(b.actual_amount) / NULLIF(SUM(b.approved_amount), 0) * 100, 1) AS execution_rate_pct
FROM eng_fact_budget_monthly b
JOIN eng_dim_project p ON p.project_id = b.project_id
JOIN eng_dim_team t ON t.team_id = b.team_id
GROUP BY b.month_label, t.department, p.project_name, b.budget_category;

CREATE OR REPLACE VIEW v_eng_throughput_monthly AS
SELECT
  DATE_FORMAT(r.create_date, '%Y-%m') AS month_label,
  t.department AS department,
  p.project_name AS project_name,
  COUNT(*) AS submitted_count,
  SUM(CASE WHEN r.delivered_flag = 1 THEN 1 ELSE 0 END) AS delivered_count,
  SUM(r.story_points) AS story_points,
  ROUND(AVG(CASE WHEN r.delivered_flag = 1 THEN r.lead_time_days END), 1) AS avg_lead_time_days
FROM eng_fact_requirement r
JOIN eng_dim_project p ON p.project_id = r.project_id
JOIN eng_dim_team t ON t.team_id = r.team_id
GROUP BY DATE_FORMAT(r.create_date, '%Y-%m'), t.department, p.project_name;

CREATE OR REPLACE VIEW v_eng_capacity_demand AS
SELECT
  c.month_label,
  t.department,
  c.role_name,
  SUM(c.headcount) AS headcount,
  ROUND(SUM(c.available_hours), 1) AS capacity_hours,
  ROUND(SUM(c.demand_hours), 1) AS demand_hours,
  ROUND(SUM(c.assigned_hours), 1) AS assigned_hours,
  ROUND(SUM(c.gap_hours), 1) AS gap_hours,
  ROUND(SUM(c.available_hours) / NULLIF(SUM(c.demand_hours), 0) * 100, 1) AS capacity_demand_ratio_pct
FROM eng_fact_capacity_monthly c
JOIN eng_dim_team t ON t.team_id = c.team_id
GROUP BY c.month_label, t.department, c.role_name;

CREATE OR REPLACE VIEW v_eng_role_distribution AS
SELECT
  t.department,
  e.role_name,
  e.employment_type,
  COALESCE(v.vendor_name, '内部自研') AS vendor_name,
  COUNT(DISTINCT e.employee_id) AS headcount,
  ROUND(SUM(e.capacity_hours_month), 1) AS capacity_hours
FROM eng_dim_employee e
JOIN eng_dim_team t ON t.team_id = e.team_id
LEFT JOIN eng_dim_vendor v ON v.vendor_id = e.vendor_id
WHERE e.active_status = '在岗'
GROUP BY t.department, e.role_name, e.employment_type, COALESCE(v.vendor_name, '内部自研');

CREATE OR REPLACE VIEW v_eng_vendor_management AS
WITH emp AS (
  SELECT vendor_id, COUNT(*) AS headcount FROM eng_dim_employee WHERE vendor_id IS NOT NULL GROUP BY vendor_id
), req AS (
  SELECT vendor_id,
         SUM(CASE WHEN delivered_flag = 1 THEN 1 ELSE 0 END) AS delivered_count,
         ROUND(AVG(CASE WHEN delivered_flag = 1 THEN lead_time_days END), 1) AS avg_lead_time_days,
         ROUND(SUM(CASE WHEN delivered_flag = 1 AND online_date <= planned_online_date THEN 1 ELSE 0 END) / NULLIF(SUM(CASE WHEN delivered_flag = 1 THEN 1 ELSE 0 END), 0) * 100, 1) AS sla_hit_rate_pct
  FROM eng_fact_requirement WHERE vendor_id IS NOT NULL GROUP BY vendor_id
), work AS (
  SELECT e.vendor_id, ROUND(SUM(w.billable_hours), 1) AS billable_hours
  FROM eng_fact_worklog_daily w JOIN eng_dim_employee e ON e.employee_id = w.employee_id
  WHERE e.vendor_id IS NOT NULL GROUP BY e.vendor_id
), code AS (
  SELECT e.vendor_id, SUM(c.lines_added - c.lines_deleted) AS net_lines
  FROM eng_fact_code_daily c JOIN eng_dim_employee e ON e.employee_id = c.employee_id
  WHERE e.vendor_id IS NOT NULL GROUP BY e.vendor_id
)
SELECT
  v.vendor_name,
  v.vendor_type,
  v.sla_level,
  COALESCE(emp.headcount, 0) AS headcount,
  COALESCE(req.delivered_count, 0) AS delivered_count,
  COALESCE(req.avg_lead_time_days, 0) AS avg_lead_time_days,
  COALESCE(req.sla_hit_rate_pct, 0) AS sla_hit_rate_pct,
  COALESCE(work.billable_hours, 0) AS billable_hours,
  ROUND(COALESCE(work.billable_hours, 0) * v.hourly_rate, 1) AS cost_amount,
  COALESCE(code.net_lines, 0) AS net_lines
FROM eng_dim_vendor v
LEFT JOIN emp ON emp.vendor_id = v.vendor_id
LEFT JOIN req ON req.vendor_id = v.vendor_id
LEFT JOIN work ON work.vendor_id = v.vendor_id
LEFT JOIN code ON code.vendor_id = v.vendor_id;

CREATE OR REPLACE VIEW v_eng_stage_aging AS
SELECT
  e.stage_name,
  r.priority,
  r.requirement_type,
  COUNT(*) AS req_count,
  ROUND(AVG(e.duration_days), 1) AS avg_days,
  ROUND(AVG(e.duration_days) + STDDEV_POP(e.duration_days) * 1.28, 1) AS p90_days
FROM eng_fact_requirement_stage_event e
JOIN eng_fact_requirement r ON r.requirement_id = e.requirement_id
WHERE e.stage_name <> {FLOW_DONE_STAGE_SQL}
GROUP BY e.stage_name, r.priority, r.requirement_type;

CREATE OR REPLACE VIEW v_eng_requirement_flow AS
SELECT
  DATE_FORMAT(s.snapshot_date, '%Y-%m-%d') AS stat_date,
  s.stage_name,
  t.department,
  p.project_name,
  r.requirement_code,
  r.priority,
  r.requirement_type,
  p.product_owner AS owner_name,
  s.age_days,
  r.demand_hours,
  1 AS req_count
FROM eng_fact_requirement_snapshot_daily s
JOIN eng_fact_requirement r ON r.requirement_id = s.requirement_id
JOIN eng_dim_project p ON p.project_id = r.project_id
JOIN eng_dim_team t ON t.team_id = r.team_id
WHERE s.snapshot_date >= '2026-04-01';

CREATE OR REPLACE VIEW v_eng_requirement_active_flow AS
SELECT *
FROM v_eng_requirement_flow
WHERE stage_name <> {FLOW_DONE_STAGE_SQL};

CREATE OR REPLACE VIEW v_eng_demand_funnel AS
SELECT
  s.stage_name,
  t.department,
  p.project_name,
  r.priority,
  COUNT(DISTINCT s.requirement_id) AS req_count,
  ROUND(AVG(s.age_days), 1) AS avg_age_days,
  SUM({STAGE_SLE_CASE_SQL}) AS over_sle_count
FROM eng_fact_requirement_snapshot_daily s
JOIN eng_fact_requirement r ON r.requirement_id = s.requirement_id
JOIN eng_dim_project p ON p.project_id = r.project_id
JOIN eng_dim_team t ON t.team_id = r.team_id
WHERE s.snapshot_date = '2026-05-28'
  AND s.stage_name <> {FLOW_DONE_STAGE_SQL}
GROUP BY s.stage_name, t.department, p.project_name, r.priority;

CREATE OR REPLACE VIEW v_eng_stage_funnel AS
SELECT
  e.stage_name,
  {STAGE_ORDER_CASE_SQL} AS stage_order,
  '2026-05-01 ~ 2026-05-28' AS period_label,
  t.department,
  p.project_name,
  COUNT(DISTINCT e.requirement_id) AS req_count,
  ROUND(AVG(e.duration_days), 1) AS avg_days,
  ROUND(AVG(e.duration_days) + STDDEV_POP(e.duration_days) * 1.28, 1) AS p90_days,
  SUM({STAGE_DURATION_SLE_CASE_SQL}) AS over_sle_count
FROM eng_fact_requirement_stage_event e
JOIN eng_fact_requirement r ON r.requirement_id = e.requirement_id
JOIN eng_dim_project p ON p.project_id = r.project_id
JOIN eng_dim_team t ON t.team_id = r.team_id
WHERE e.entered_date BETWEEN '2026-05-01' AND '2026-05-28'
  AND e.stage_name <> {FLOW_DONE_STAGE_SQL}
GROUP BY e.stage_name, t.department, p.project_name;

CREATE OR REPLACE VIEW v_eng_efficiency_matrix AS
WITH req AS (
  SELECT project_id, COUNT(*) AS delivered_requirements, ROUND(SUM(demand_hours), 1) AS demand_hours
  FROM eng_fact_requirement
  WHERE delivered_flag = 1 AND online_date >= '2026-05-01'
  GROUP BY project_id
), work AS (
  SELECT project_id,
         ROUND(SUM(hours), 1) AS capacity_hours,
         ROUND(SUM(hours) / 8, 1) AS workforce_days
  FROM eng_fact_worklog_daily
  WHERE work_date >= '2026-05-01'
  GROUP BY project_id
)
SELECT
  t.department,
  t.product_line AS team_name,
  p.project_name,
  ROUND(COALESCE(work.workforce_days, 0), 1) AS workforce_days,
  COALESCE(req.delivered_requirements, 0) AS delivered_requirements,
  ROUND(COALESCE(req.delivered_requirements, 0) / NULLIF(COALESCE(work.workforce_days, 0), 0), 3) AS efficiency_ratio,
  CASE
    WHEN ROUND(COALESCE(req.delivered_requirements, 0) / NULLIF(COALESCE(work.workforce_days, 0), 0), 3) >= 0.09 THEN '高效率'
    WHEN ROUND(COALESCE(req.delivered_requirements, 0) / NULLIF(COALESCE(work.workforce_days, 0), 0), 3) >= 0.05 THEN '中效率'
    ELSE '低效率'
  END AS efficiency_level,
  COALESCE(work.capacity_hours, 0) AS capacity_hours,
  COALESCE(req.demand_hours, 0) AS demand_hours
FROM eng_dim_project p
JOIN eng_dim_team t ON t.team_id = p.team_id
LEFT JOIN req ON req.project_id = p.project_id
LEFT JOIN work ON work.project_id = p.project_id
WHERE COALESCE(work.workforce_days, 0) > 0 OR COALESCE(req.delivered_requirements, 0) > 0;

CREATE OR REPLACE VIEW v_eng_work_code_daily AS
SELECT stat_date, department, project_name, person_name, role_name, work_type,
       ROUND(SUM(total_hours), 1) AS total_hours,
       SUM(commit_count) AS commit_count,
       SUM(net_lines) AS net_lines
FROM (
  SELECT DATE_FORMAT(w.work_date, '%Y-%m-%d') AS stat_date, t.department, p.project_name,
         e.employee_name AS person_name, e.role_name, w.work_type,
         SUM(w.hours) AS total_hours, 0 AS commit_count, 0 AS net_lines
  FROM eng_fact_worklog_daily w
  JOIN eng_dim_employee e ON e.employee_id = w.employee_id
  JOIN eng_dim_team t ON t.team_id = e.team_id
  JOIN eng_dim_project p ON p.project_id = w.project_id
  GROUP BY DATE_FORMAT(w.work_date, '%Y-%m-%d'), t.department, p.project_name, e.employee_name, e.role_name, w.work_type
  UNION ALL
  SELECT DATE_FORMAT(c.code_date, '%Y-%m-%d') AS stat_date, t.department, p.project_name,
         e.employee_name AS person_name, e.role_name, CONCAT('代码-', c.category) AS work_type,
         0 AS total_hours, SUM(c.commit_count) AS commit_count, SUM(c.lines_added - c.lines_deleted) AS net_lines
  FROM eng_fact_code_daily c
  JOIN eng_dim_employee e ON e.employee_id = c.employee_id
  JOIN eng_dim_team t ON t.team_id = e.team_id
  JOIN eng_dim_project p ON p.project_id = c.project_id
  GROUP BY DATE_FORMAT(c.code_date, '%Y-%m-%d'), t.department, p.project_name, e.employee_name, e.role_name, CONCAT('代码-', c.category)
) z
GROUP BY stat_date, department, project_name, person_name, role_name, work_type;

CREATE OR REPLACE VIEW v_eng_work_type_summary AS
SELECT work_type, department,
       ROUND(SUM(total_hours), 1) AS total_hours,
       SUM(commit_count) AS commit_count,
       SUM(net_lines) AS net_lines
FROM v_eng_work_code_daily
GROUP BY work_type, department;

CREATE OR REPLACE VIEW v_eng_project_drill AS
WITH req AS (
  SELECT project_id,
         SUM(CASE WHEN status NOT IN ('已上线','暂缓') THEN 1 ELSE 0 END) AS active_reqs,
         SUM(CASE WHEN delivered_flag = 1 THEN 1 ELSE 0 END) AS delivered_reqs,
         ROUND(SUM(demand_hours), 1) AS demand_hours,
         ROUND(AVG(CASE WHEN delivered_flag = 1 THEN lead_time_days END), 1) AS avg_lead_time_days
  FROM eng_fact_requirement GROUP BY project_id
), work AS (
  SELECT project_id, ROUND(SUM(hours), 1) AS work_hours FROM eng_fact_worklog_daily GROUP BY project_id
), code AS (
  SELECT project_id, SUM(lines_added - lines_deleted) AS net_lines FROM eng_fact_code_daily GROUP BY project_id
)
SELECT
  t.department,
  p.project_code,
  p.project_name,
  p.priority,
  p.product_owner,
  COALESCE(req.active_reqs, 0) AS active_reqs,
  COALESCE(req.delivered_reqs, 0) AS delivered_reqs,
  COALESCE(req.demand_hours, 0) AS demand_hours,
  COALESCE(work.work_hours, 0) AS work_hours,
  COALESCE(code.net_lines, 0) AS net_lines,
  COALESCE(req.avg_lead_time_days, 0) AS avg_lead_time_days,
  CASE
    WHEN COALESCE(req.active_reqs, 0) >= 12 OR COALESCE(req.avg_lead_time_days, 0) >= 40 THEN '高'
    WHEN COALESCE(req.active_reqs, 0) >= 7 OR COALESCE(req.avg_lead_time_days, 0) >= 28 THEN '中'
    ELSE '低'
  END AS risk_level
FROM eng_dim_project p
JOIN eng_dim_team t ON t.team_id = p.team_id
LEFT JOIN req ON req.project_id = p.project_id
LEFT JOIN work ON work.project_id = p.project_id
LEFT JOIN code ON code.project_id = p.project_id;

CREATE OR REPLACE VIEW v_eng_person_drill AS
WITH work AS (
  SELECT employee_id, ROUND(SUM(hours), 1) AS work_hours, COUNT(DISTINCT requirement_id) AS requirement_count
  FROM eng_fact_worklog_daily GROUP BY employee_id
), code AS (
  SELECT employee_id, SUM(commit_count) AS commit_count, SUM(lines_added - lines_deleted) AS net_lines
  FROM eng_fact_code_daily GROUP BY employee_id
)
SELECT
  t.department,
  e.employee_name AS person_name,
  e.role_name,
  e.employment_type,
  COALESCE(v.vendor_name, '内部自研') AS vendor_name,
  COALESCE(work.work_hours, 0) AS work_hours,
  COALESCE(code.commit_count, 0) AS commit_count,
  COALESCE(code.net_lines, 0) AS net_lines,
  COALESCE(work.requirement_count, 0) AS requirement_count,
  ROUND(COALESCE(work.work_hours, 0) / NULLIF(e.capacity_hours_month * 5, 0) * 100, 1) AS utilization_rate_pct
FROM eng_dim_employee e
JOIN eng_dim_team t ON t.team_id = e.team_id
LEFT JOIN eng_dim_vendor v ON v.vendor_id = e.vendor_id
LEFT JOIN work ON work.employee_id = e.employee_id
LEFT JOIN code ON code.employee_id = e.employee_id
WHERE e.active_status = '在岗';
"""


def metadata_cleanup_sql():
    return """
USE crest;
DELETE FROM snapshot_core_chart_view WHERE scene_id BETWEEN 980001 AND 980005 OR id BETWEEN 980000 AND 980999;
DELETE FROM core_chart_view WHERE scene_id BETWEEN 980001 AND 980005 OR id BETWEEN 980000 AND 980999;
DELETE FROM snapshot_data_visualization_info WHERE id IN ('980001','980002','980003','980004','980005');
DELETE FROM data_visualization_info WHERE id IN ('980001','980002','980003','980004','980005');
DELETE FROM core_dataset_table_field WHERE dataset_group_id BETWEEN 982000 AND 982999 OR dataset_table_id BETWEEN 983000 AND 983999 OR id BETWEEN 984000000 AND 984999999;
DELETE FROM core_dataset_table WHERE dataset_group_id BETWEEN 982000 AND 982999 OR id BETWEEN 983000 AND 983999;
DELETE FROM core_dataset_group WHERE id BETWEEN 982000 AND 982999;
"""


def snapshot_sql():
    return """
INSERT INTO crest.snapshot_core_chart_view SELECT * FROM crest.core_chart_view WHERE scene_id BETWEEN 980001 AND 980005;
INSERT INTO crest.snapshot_data_visualization_info SELECT * FROM crest.data_visualization_info WHERE id IN ('980001','980002','980003','980004','980005');

DELETE FROM core_opt_recent
WHERE uid = 1
  AND resource_id IN (910001, 980001, 980002, 980003, 980004, 980005, 982001, 982002, 982003, 982004, 982005, 982006, 982007);

INSERT INTO core_opt_recent (id, resource_id, resource_name, uid, resource_type, opt_type, time) VALUES
(1780032200000000001, 980001, NULL, 1, 1, 2, 1780032200000),
(1780032200000000002, 980002, NULL, 1, 1, 2, 1780032190000),
(1780032200000000003, 980003, NULL, 1, 1, 2, 1780032180000),
(1780032200000000004, 980004, NULL, 1, 1, 2, 1780032170000),
(1780032200000000005, 980005, NULL, 1, 1, 2, 1780032160000),
(1780032200000000006, 982001, NULL, 1, 4, 2, 1780032150000),
(1780032200000000007, 982002, NULL, 1, 4, 2, 1780032140000),
(1780032200000000008, 982003, NULL, 1, 4, 2, 1780032130000),
(1780032200000000009, 982004, NULL, 1, 4, 2, 1780032120000),
(1780032200000000010, 982005, NULL, 1, 4, 2, 1780032110000),
(1780032200000000011, 982006, NULL, 1, 4, 2, 1780032100000),
(1780032200000000012, 982007, NULL, 1, 4, 2, 1780032090000),
(1780032200000000013, 910001, NULL, 1, 5, 2, 1780032080000);
"""


def build_sql():
    sql = [DDL]
    sql += insert_many(
        "eng_dim_team",
        ["team_id", "department", "product_line", "tribe", "manager_name", "cost_center"],
        TEAMS,
    )
    sql += insert_many(
        "eng_dim_vendor",
        ["vendor_id", "vendor_name", "vendor_type", "region", "contract_mode", "sla_level", "hourly_rate", "active_status"],
        VENDORS,
    )
    sql += insert_many(
        "eng_dim_employee",
        [
            "employee_id",
            "employee_name",
            "team_id",
            "vendor_id",
            "employment_type",
            "role_name",
            "seniority",
            "cost_rate",
            "start_date",
            "capacity_hours_month",
            "active_status",
        ],
        EMPLOYEES,
    )
    sql += insert_many(
        "eng_dim_project",
        ["project_id", "project_code", "project_name", "team_id", "business_domain", "priority", "product_owner"],
        PROJECTS,
    )
    sql += insert_many(
        "eng_fact_requirement",
        [
            "requirement_id",
            "requirement_code",
            "project_id",
            "team_id",
            "vendor_id",
            "requirement_type",
            "priority",
            "status",
            "create_date",
            "planned_online_date",
            "online_date",
            "lead_time_days",
            "discovery_days",
            "design_days",
            "dev_days",
            "test_days",
            "waiting_days",
            "rework_count",
            "story_points",
            "demand_hours",
            "delivered_flag",
        ],
        REQUIREMENTS,
    )
    sql += insert_many(
        "eng_fact_worklog_daily",
        ["worklog_id", "work_date", "employee_id", "project_id", "requirement_id", "work_type", "hours", "billable_hours"],
        WORKLOGS,
    )
    sql += insert_many(
        "eng_fact_code_daily",
        [
            "code_id",
            "code_date",
            "employee_id",
            "project_id",
            "requirement_id",
            "repo_name",
            "language",
            "category",
            "commit_count",
            "lines_added",
            "lines_deleted",
            "files_changed",
        ],
        CODE_DAILY,
    )
    sql += insert_many(
        "eng_fact_capacity_monthly",
        ["capacity_id", "month_label", "team_id", "role_name", "headcount", "available_hours", "demand_hours", "assigned_hours", "gap_hours"],
        CAPACITY,
    )
    sql += insert_many(
        "eng_fact_requirement_stage_event",
        ["event_id", "requirement_id", "stage_name", "stage_order", "entered_date", "left_date", "duration_days"],
        STAGE_EVENTS,
    )
    sql += insert_many(
        "eng_fact_requirement_snapshot_daily",
        ["snapshot_id", "snapshot_date", "requirement_id", "stage_name", "stage_order", "stage_entered_date", "age_days", "completed_flag"],
        REQUIREMENT_SNAPSHOTS,
    )
    sql += insert_many(
        "eng_fact_deployment",
        [
            "deployment_id",
            "requirement_id",
            "project_id",
            "team_id",
            "deploy_date",
            "environment",
            "deployment_status",
            "change_lead_time_hours",
            "failed_flag",
            "recovery_time_hours",
            "rework_deploy_flag",
            "emergency_flag",
            "batch_size",
        ],
        DEPLOYMENTS,
    )
    sql += insert_many(
        "eng_fact_defect",
        [
            "defect_id",
            "requirement_id",
            "project_id",
            "team_id",
            "vendor_id",
            "defect_phase",
            "severity",
            "found_date",
            "resolved_date",
            "escaped_flag",
        ],
        DEFECTS,
    )
    sql += insert_many(
        "eng_fact_incident",
        [
            "incident_id",
            "defect_id",
            "deployment_id",
            "project_id",
            "team_id",
            "opened_date",
            "severity",
            "recovery_time_hours",
            "root_cause",
        ],
        INCIDENTS,
    )
    sql += insert_many(
        "eng_fact_budget_monthly",
        ["budget_id", "month_label", "project_id", "team_id", "budget_category", "approved_amount", "actual_amount"],
        BUDGET_ROWS,
    )
    sql += insert_many(
        "eng_metric_definition",
        [
            "metric_id",
            "metric_code",
            "domain_name",
            "metric_name",
            "source_system",
            "formula_text",
            "grain",
            "standard_basis",
            "refresh_frequency",
            "caveat",
        ],
        METRIC_DEFINITIONS,
    )
    sql.append(VIEWS)
    sql.append(metadata_cleanup_sql())
    sql += dataset_sql()
    sql += chart_sql()
    sql += screen_sql()
    sql.append(snapshot_sql())
    return sql


def emit_sql(add):
    for statement in build_sql():
        add(statement)


def main():
    print("\n\n".join(build_sql()))


if __name__ == "__main__":
    main()
