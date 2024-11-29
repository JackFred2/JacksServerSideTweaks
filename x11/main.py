import re

def main():

    cols = dict()

    with open("rgb.txt", mode="r", encoding="utf-8") as f:
        for line in f:
            match = re.search(" *([0-9]{1,3}) +([0-9]{1,3}) +([0-9]{1,3}) ?\t+([^\t\n]*)", line)
            if match and len(match.groups()) >= 4:
                r = int(match.group(1))
                g = int(match.group(2))
                b = int(match.group(3))
                name = match.group(4)

                name = name.replace(" ", "").lower()

                cols[name] = (r << 16) + (g << 8) + b

    print(cols)

    with open("map.txt", mode = "w", encoding="utf-8") as f:
        f.write("""
// Generated via script on repo
static Map<String, Integer> makeX11() {
    Map<String, Integer> map = new HashMap();
""")

        for name, col in dict(sorted(cols.items())).items():
            hex_str = f"{col:06X}"
            f.write(f"    map.put(\"{name}\", 0x{hex_str});\n")

        f.write("    return map;\n")
        f.write("}")



if __name__ == "__main__":
    main()