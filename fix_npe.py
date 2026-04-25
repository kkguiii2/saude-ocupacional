path = r'c:\Users\kkguiii\Desktop\Microservice\Amb\src\main\java\com\industrial\saude\service\RelatorioService.java'
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('colab.getSetor().name()', 'colab.getSetor() != null ? colab.getSetor().name() : ""')
content = content.replace('colab.getTipoRisco().name()', 'colab.getTipoRisco() != null ? colab.getTipoRisco().name() : ""')
content = content.replace('colab.getStatusFuncionario().name()', 'colab.getStatusFuncionario() != null ? colab.getStatusFuncionario().name() : ""')

content = content.replace('c.getSetor().name()', 'c.getSetor() != null ? c.getSetor().name() : ""')
content = content.replace('c.getTipoRisco().name()', 'c.getTipoRisco() != null ? c.getTipoRisco().name() : ""')
content = content.replace('c.getStatusFuncionario().name()', 'c.getStatusFuncionario() != null ? c.getStatusFuncionario().name() : ""')

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Done")
