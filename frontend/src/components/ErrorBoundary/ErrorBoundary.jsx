import { Component } from 'react'
import './ErrorBoundary.css'

export default class ErrorBoundary extends Component {
  constructor(props) {
    super(props)
    this.state = { hasError: false, error: null }
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error }
  }

  componentDidCatch(error, errorInfo) {
    if (import.meta.env.DEV) {
      console.error('ErrorBoundary caught:', error, errorInfo)
    }
  }

  handleRetry = () => {
    this.setState({ hasError: false, error: null })
  }

  handleGoHome = () => {
    this.setState({ hasError: false, error: null })
    window.location.href = '/'
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="error-boundary">
          <div className="error-boundary-card">
            <div className="error-boundary-icon">
              <i className="fas fa-exclamation-triangle"></i>
            </div>
            <h1 className="error-boundary-titulo">Algo salió mal</h1>
            <p className="error-boundary-mensaje">
              Ha ocurrido un error inesperado. Por favor, intenta de nuevo.
            </p>
            {this.state.error && (
              <details className="error-boundary-detalle">
                <summary>Detalles del error</summary>
                <pre>{this.state.error.message}</pre>
              </details>
            )}
            <div className="error-boundary-acciones">
              <button className="btn-primario" onClick={this.handleRetry}>
                <i className="fas fa-redo"></i> Reintentar
              </button>
              <button className="btn-secundario" onClick={this.handleGoHome}>
                <i className="fas fa-home"></i> Ir al inicio
              </button>
            </div>
          </div>
        </div>
      )
    }

    return this.props.children
  }
}
