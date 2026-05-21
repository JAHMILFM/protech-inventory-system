package com.ferreteria.protech.dto;

import com.ferreteria.protech.model.Product;
import java.math.BigDecimal;

/**
 * DTO para transferencia de datos de Producto en respuestas API.
 */
public class ProductDTO {

    private Long id;
    private String sku;
    private String ean13;
    private String nombre;
    private String descripcion;
    private BigDecimal precioCosto;
    private BigDecimal precioVenta;
    private BigDecimal precioOferta;
    private BigDecimal precioConIgv;
    private BigDecimal margenGanancia;
    private Integer stockActual;
    private Integer stockMinimo;
    private Integer stockReserva;
    private BigDecimal largoCm;
    private BigDecimal anchoCm;
    private BigDecimal altoCm;
    private BigDecimal pesoKg;
    private String ubicacionPasillo;
    private String ubicacionLado;
    private String ubicacionNivel;
    private String ubicacionCompleta;
    private String marca;
    private String modelo;
    private String unidadMedida;
    private String imagenUrl;
    private Boolean activo;
    private boolean stockCritico;
    private Long categoriaId;
    private String categoriaNombre;
    private Long proveedorId;
    private String proveedorNombre;
    private String createdAt;
    private String updatedAt;

    /** Construye un DTO desde una entidad Product */
    public static ProductDTO fromEntity(Product p) {
        ProductDTO dto = new ProductDTO();
        dto.id = p.getId();
        dto.sku = p.getSku();
        dto.ean13 = p.getEan13();
        dto.nombre = p.getNombre();
        dto.descripcion = p.getDescripcion();
        dto.precioCosto = p.getPrecioCosto();
        dto.precioVenta = p.getPrecioVenta();
        dto.precioOferta = p.getPrecioOferta();
        dto.precioConIgv = p.getPrecioConIgv();
        dto.margenGanancia = p.getMargenGanancia();
        dto.stockActual = p.getStockActual();
        dto.stockMinimo = p.getStockMinimo();
        dto.stockReserva = p.getStockReserva();
        dto.largoCm = p.getLargoCm();
        dto.anchoCm = p.getAnchoCm();
        dto.altoCm = p.getAltoCm();
        dto.pesoKg = p.getPesoKg();
        dto.ubicacionPasillo = p.getUbicacionPasillo();
        dto.ubicacionLado = p.getUbicacionLado();
        dto.ubicacionNivel = p.getUbicacionNivel();
        dto.ubicacionCompleta = p.getUbicacionCompleta();
        dto.marca = p.getMarca();
        dto.modelo = p.getModelo();
        dto.unidadMedida = p.getUnidadMedida();
        dto.imagenUrl = p.getImagenUrl();
        dto.activo = p.getActivo();
        dto.stockCritico = p.isStockCritico();
        if (p.getCategoria() != null) {
            dto.categoriaId = p.getCategoria().getId();
            dto.categoriaNombre = p.getCategoria().getNombre();
        }
        if (p.getProveedor() != null) {
            dto.proveedorId = p.getProveedor().getId();
            dto.proveedorNombre = p.getProveedor().getNombreCompleto();
        }
        dto.createdAt = p.getCreatedAt() != null ? p.getCreatedAt().toString() : null;
        dto.updatedAt = p.getUpdatedAt() != null ? p.getUpdatedAt().toString() : null;
        return dto;
    }

    // ── Getters y Setters ───────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getEan13() { return ean13; }
    public void setEan13(String ean13) { this.ean13 = ean13; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public BigDecimal getPrecioCosto() { return precioCosto; }
    public void setPrecioCosto(BigDecimal precioCosto) { this.precioCosto = precioCosto; }

    public BigDecimal getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(BigDecimal precioVenta) { this.precioVenta = precioVenta; }

    public BigDecimal getPrecioOferta() { return precioOferta; }
    public void setPrecioOferta(BigDecimal precioOferta) { this.precioOferta = precioOferta; }

    public BigDecimal getPrecioConIgv() { return precioConIgv; }
    public void setPrecioConIgv(BigDecimal precioConIgv) { this.precioConIgv = precioConIgv; }

    public BigDecimal getMargenGanancia() { return margenGanancia; }
    public void setMargenGanancia(BigDecimal margenGanancia) { this.margenGanancia = margenGanancia; }

    public Integer getStockActual() { return stockActual; }
    public void setStockActual(Integer stockActual) { this.stockActual = stockActual; }

    public Integer getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(Integer stockMinimo) { this.stockMinimo = stockMinimo; }

    public Integer getStockReserva() { return stockReserva; }
    public void setStockReserva(Integer stockReserva) { this.stockReserva = stockReserva; }

    public BigDecimal getLargoCm() { return largoCm; }
    public void setLargoCm(BigDecimal largoCm) { this.largoCm = largoCm; }

    public BigDecimal getAnchoCm() { return anchoCm; }
    public void setAnchoCm(BigDecimal anchoCm) { this.anchoCm = anchoCm; }

    public BigDecimal getAltoCm() { return altoCm; }
    public void setAltoCm(BigDecimal altoCm) { this.altoCm = altoCm; }

    public BigDecimal getPesoKg() { return pesoKg; }
    public void setPesoKg(BigDecimal pesoKg) { this.pesoKg = pesoKg; }

    public String getUbicacionPasillo() { return ubicacionPasillo; }
    public void setUbicacionPasillo(String ubicacionPasillo) { this.ubicacionPasillo = ubicacionPasillo; }

    public String getUbicacionLado() { return ubicacionLado; }
    public void setUbicacionLado(String ubicacionLado) { this.ubicacionLado = ubicacionLado; }

    public String getUbicacionNivel() { return ubicacionNivel; }
    public void setUbicacionNivel(String ubicacionNivel) { this.ubicacionNivel = ubicacionNivel; }

    public String getUbicacionCompleta() { return ubicacionCompleta; }
    public void setUbicacionCompleta(String ubicacionCompleta) { this.ubicacionCompleta = ubicacionCompleta; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public boolean isStockCritico() { return stockCritico; }
    public void setStockCritico(boolean stockCritico) { this.stockCritico = stockCritico; }

    public Long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Long categoriaId) { this.categoriaId = categoriaId; }

    public String getCategoriaNombre() { return categoriaNombre; }
    public void setCategoriaNombre(String categoriaNombre) { this.categoriaNombre = categoriaNombre; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public Long getProveedorId() { return proveedorId; }
    public void setProveedorId(Long proveedorId) { this.proveedorId = proveedorId; }

    public String getProveedorNombre() { return proveedorNombre; }
    public void setProveedorNombre(String proveedorNombre) { this.proveedorNombre = proveedorNombre; }
}
